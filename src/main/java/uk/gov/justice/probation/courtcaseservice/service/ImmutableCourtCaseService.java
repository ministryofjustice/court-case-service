package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CourtCaseMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class ImmutableCourtCaseService implements CourtCaseService {

    private final CourtRepository courtRepository;
    private final CourtCaseRepository courtCaseRepository;
    private final TelemetryService telemetryService;
    private final GroupedOffenderMatchRepository matchRepository;
    private final boolean globalProbationStatusUpdate;

    @Autowired
    public ImmutableCourtCaseService(CourtRepository courtRepository,
                                     CourtCaseRepository courtCaseRepository,
                                     TelemetryService telemetryService,
                                     GroupedOffenderMatchRepository matchRepository,
                                     @Value("${feature.flags.global-probation-status-update:true}") boolean globalProbationStatusUpdate) {
        this.courtRepository = courtRepository;
        this.courtCaseRepository = courtCaseRepository;
        this.telemetryService = telemetryService;
        this.matchRepository = matchRepository;
        this.globalProbationStatusUpdate = globalProbationStatusUpdate;
    }

    @Override
    public Mono<CourtCaseEntity> createCase(String caseId, CourtCaseEntity updatedCase) throws EntityNotFoundException, InputMismatchException {
        validateEntity(caseId, updatedCase);
        courtCaseRepository.findFirstByCaseIdOrderByIdDesc(caseId)
                .ifPresentOrElse(
                        (existingCase) -> {
                            updatedCase.getDefendants()
                                    .forEach(defendantEntity ->
                                            updateOffenderMatches(existingCase, updatedCase, defendantEntity.getDefendantId())
                                    );
                            trackUpdateEvents(updatedCase, existingCase);
                        },
                        () -> trackCreateEvents(updatedCase));

        return Mono.just(updatedCase)
                .map((courtCaseEntity) -> {
                    log.debug("Saving case ID {}", caseId);
                    return courtCaseRepository.save(courtCaseEntity);
                })
                .doAfterTerminate(() -> updateOtherProbationStatusForCrnByCaseId(updatedCase.getCrn(), updatedCase.getProbationStatus(), updatedCase.getCaseId()));
    }

    @Override
    public Mono<CourtCaseEntity> createUpdateCaseForSingleDefendantId(String caseId, String defendantId, CourtCaseEntity updatedCase)
            throws EntityNotFoundException, InputMismatchException {
        validateEntityByDefendantId(caseId, defendantId, updatedCase);
        // The case to be saved might change from the one passed in, through the addition of defendants
        var caseToSave = courtCaseRepository.findByCaseIdAndDefendantId(caseId, defendantId)
                .map((existingCase) -> {
                    // Copy existing case defendants to updated case
                    updateOffenderMatches(existingCase, updatedCase, defendantId);
                    trackUpdateEvents(updatedCase, existingCase);
                    return CourtCaseMapper.mergeDefendantsOnCase(existingCase, updatedCase, defendantId);
                })
                .orElseGet(() -> {
                    trackCreateEvents(updatedCase);
                    return updatedCase;
                });

        return Mono.just(caseToSave)
                .map((courtCaseEntity) -> {
                    log.debug("Saving case ID {} with updates applied for defendant ID {}", caseId, defendantId);
                    return courtCaseRepository.save(courtCaseEntity);
                })
                .doAfterTerminate(() -> updateOtherProbationStatusForCrnByCaseId(updatedCase.getCrn(), updatedCase.getProbationStatus(), updatedCase.getCaseId()));
    }


    void updateOtherProbationStatusForCrn(String crn, String probationStatus, String caseNo) {
        if (crn != null) {
            final var courtCases = courtCaseRepository.findOtherCurrentCasesByCrn(crn, caseNo)
                    .stream()
                    .filter(courtCaseEntity -> !courtCaseEntity.getProbationStatus().equalsIgnoreCase(probationStatus))
                    .map(courtCaseEntity -> CourtCaseMapper.create(courtCaseEntity, crn, probationStatus))
                    .collect(Collectors.toList());

            if (!courtCases.isEmpty()) {
                log.debug("Updating {} cases for CRN {} with changed probation status to {}", courtCases.size(), crn, probationStatus);
                courtCaseRepository.saveAll(courtCases);
            }
        }
    }

    void updateOtherProbationStatusForCrnByCaseId(String crn, String probationStatus, String caseId) {
        // Temporary hotfix - This code is killing DB performance so optionally skipping until performance issue is resolved
        if (crn == null || !globalProbationStatusUpdate) {
            return;
        }

        final var courtCases = courtCaseRepository.findOtherCurrentCasesByCrnNotCaseId(crn, caseId)
                .stream()
                .filter(courtCaseEntity -> hasAnyDefendantsSameCrnDifferentProbationStatus(courtCaseEntity.getDefendants(), crn, probationStatus))
                .map(courtCaseEntity -> CourtCaseMapper.create(courtCaseEntity, crn, probationStatus))
                .collect(Collectors.toList());

        if (!courtCases.isEmpty()) {
            log.debug("Updating {} cases for CRN {} with changed probation status to {}", courtCases.size(), crn, probationStatus);
            courtCaseRepository.saveAll(courtCases);
        }
    }

    boolean hasAnyDefendantsSameCrnDifferentProbationStatus(List<DefendantEntity> defendants, String crn, String probationStatus) {
        return Optional.ofNullable(defendants).orElse(Collections.emptyList())
            .stream()
            .anyMatch(defendant -> Objects.equals(defendant.getCrn(), crn) && !Objects.equals(defendant.getProbationStatus(), probationStatus));
    }

    private void trackCreateEvents(CourtCaseEntity createdCase) {
        telemetryService.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, createdCase);
        if (createdCase.getCrn() != null)
            telemetryService.trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, createdCase);
    }

    private void trackUpdateEvents(CourtCaseEntity updatedCase, CourtCaseEntity existingCase) {
        telemetryService.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCase);
        if (existingCase.getCrn() == null && updatedCase.getCrn() != null)
            telemetryService.trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, updatedCase);
        else if (existingCase.getCrn() != null & updatedCase.getCrn() == null)
            telemetryService.trackCourtCaseEvent(TelemetryEventType.DEFENDANT_UNLINKED, existingCase);
    }

    @Override
    public CourtCaseEntity getCaseByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException {
        checkCourtExists(courtCode);
        log.info("Court case requested for court {} for case {}", courtCode, caseNo);
        return courtCaseRepository.findByCourtCodeAndCaseNo(courtCode, caseNo)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found for court %s", caseNo, courtCode)));
    }

    @Override
    public CourtCaseEntity getCaseByCaseId(String caseId) throws EntityNotFoundException {
        log.info("Court case requested for case ID {}", caseId);
        return courtCaseRepository.findByCaseId(caseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found", caseId)));
    }

    @Override
    public CourtCaseEntity getCaseByCaseIdAndDefendantId(String caseId, String defendantId) throws EntityNotFoundException {
        log.info("Court case requested for case ID {} and defendant ID {}", caseId, defendantId);
        return courtCaseRepository.findByCaseIdAndDefendantId(caseId, defendantId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found for defendant %s", caseId, defendantId)));
    }

    @Override
    public List<CourtCaseEntity> filterCases(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore) {
        final var court = courtRepository.findByCourtCode(courtCode)
                .orElseThrow(() -> new EntityNotFoundException("Court %s not found", courtCode));

        return courtCaseRepository.findByCourtCodeAndHearingDay(court.getCourtCode(), hearingDay, createdAfter, createdBefore);
    }

    private void validateEntity(String caseId, CourtCaseEntity updatedCase) {
        checkCourtExists(updatedCase.getHearings().get(0).getCourtCode());
        checkEntityCaseIdAgree(caseId, updatedCase);
    }

    private void validateEntityByDefendantId(String caseId, String defendantId, CourtCaseEntity updatedCase) {
        validateEntity(caseId, updatedCase);

        checkEntityDefendantIdExists(defendantId, updatedCase);
    }

    private void checkEntityCaseIdAgree(String caseId, CourtCaseEntity updatedCase) {
        if (!caseId.equals(updatedCase.getCaseId())) {
            throw new ConflictingInputException(String.format("Case Id %s does not match with value from body %s",
                    caseId, updatedCase.getCaseId()));
        }
    }

    private void checkEntityDefendantIdExists(String defendantId, CourtCaseEntity updatedCase) {
        var defendants = Optional.ofNullable(updatedCase.getDefendants()).orElse(Collections.emptyList());
        // Should not be possible because the updatedCase is built from the CourtCaseEntity with one defendant
        if (defendants.size() != 1) {
            throw new IllegalArgumentException(String.format("More than one defendant submitted on the CourtCaseEntity for single defendant update %s", defendantId));
        }
        final var entityDefendantId = defendants.get(0).getDefendantId();
        if (!defendantId.equalsIgnoreCase(entityDefendantId)) {
            throw new ConflictingInputException(String.format("Defendant Id %s does not match the one in the CourtCaseEntity body as submitted %s",
                    defendantId, entityDefendantId));
        }
    }

    private void checkCourtExists(String courtCode) throws EntityNotFoundException {
        courtRepository.findByCourtCode(courtCode)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Court %s not found", courtCode)));
    }

    private void updateOffenderMatches(CourtCaseEntity existingCase, CourtCaseEntity updatedCase, String defendantId) {
        final var groupedMatches = matchRepository.findByCaseIdAndDefendantId(updatedCase.getCaseId(), defendantId);
        groupedMatches
                .map(GroupedOffenderMatchesEntity::getOffenderMatches)
                .ifPresent(matches -> matches
                        .forEach(match -> confirmAndRejectMatches(existingCase, updatedCase, match, defendantId)));
        groupedMatches.ifPresent(matchRepository::save);
    }

    private void confirmAndRejectMatches(CourtCaseEntity existingCase, CourtCaseEntity updatedCase, OffenderMatchEntity match, String defendantId) {
        boolean crnMatches = match.getCrn().equals(updatedCase.getCrn());
        match.setConfirmed(crnMatches);
        match.setRejected(!crnMatches);
        if (crnMatches) {
            telemetryService.trackMatchEvent(TelemetryEventType.MATCH_CONFIRMED, match, updatedCase, defendantId);
        } else {
            telemetryService.trackMatchEvent(TelemetryEventType.MATCH_REJECTED, match, updatedCase, defendantId);
        }

        if (crnMatches && updatedCase.getPnc() != null && !updatedCase.getPnc().equals(match.getPnc())) {
            log.warn(String.format("Unexpected PNC mismatch when updating offender match - matchId: '%s', crn: '%s', matchPnc: %s, updatePnc: %s",
                    match.getId(), existingCase.getCrn(), match.getPnc(), existingCase.getPnc()));
        }
        if (crnMatches && updatedCase.getCro() != null && !updatedCase.getCro().equals(match.getCro())) {
            log.warn(String.format("Unexpected CRO mismatch when updating offender match - matchId: '%s', crn: '%s', matchCro: %s, updateCro: %s",
                    match.getId(), existingCase.getCrn(), match.getCro(), existingCase.getCro()));
        }
    }

    public Optional<LocalDateTime> filterCasesLastModified(String courtCode, LocalDate searchDate) {
        return courtCaseRepository.findLastModifiedByHearingDay(courtCode, searchDate);
    }

}
