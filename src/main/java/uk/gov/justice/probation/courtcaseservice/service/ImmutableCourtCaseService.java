package uk.gov.justice.probation.courtcaseservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CourtCaseMapper;

@Service
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class ImmutableCourtCaseService implements CourtCaseService {

    private final CourtRepository courtRepository;
    private final CourtCaseRepository courtCaseRepository;
    private final TelemetryService telemetryService;
    private final GroupedOffenderMatchRepository matchRepository;
    private final boolean caseListExtended;

    @Autowired
    public ImmutableCourtCaseService(CourtRepository courtRepository,
                                    CourtCaseRepository courtCaseRepository,
                                    TelemetryService telemetryService,
                                    GroupedOffenderMatchRepository matchRepository,
                                    @Value("${feature.flags.case-list-extended:false}") boolean caseListExtended) {
        this.courtRepository = courtRepository;
        this.courtCaseRepository = courtCaseRepository;
        this.telemetryService = telemetryService;
        this.matchRepository = matchRepository;
        this.caseListExtended = caseListExtended;
    }

    @Override
    public Mono<CourtCaseEntity> createCase(String caseId, CourtCaseEntity updatedCase) throws EntityNotFoundException, InputMismatchException {
        validateEntity(caseId, updatedCase);
        courtCaseRepository.findFirstByCaseIdOrderByIdDesc(caseId)
            .ifPresentOrElse(
                (existingCase) -> {
                    updateOffenderMatches(existingCase, updatedCase);
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
    public Mono<CourtCaseEntity> createCase(String courtCode, String caseNo, CourtCaseEntity updatedCase) throws EntityNotFoundException, InputMismatchException {
        validateEntity(courtCode, caseNo, updatedCase);
        courtCaseRepository.findFirstByCaseNoOrderByCreatedDesc(courtCode, caseNo)
                .ifPresentOrElse(
                        (existingCase) -> {
                            updateOffenderMatches(existingCase, updatedCase);
                            trackUpdateEvents(updatedCase, existingCase);
                        },
                        () -> trackCreateEvents(updatedCase));

        return Mono.just(updatedCase)
            .map((courtCaseEntity) -> {
                log.debug("Saving case {} for court {}", caseNo, courtCode);
                return courtCaseRepository.save(courtCaseEntity);
            })
            .doAfterTerminate(() -> updateOtherProbationStatusForCrn(updatedCase.getCrn(), updatedCase.getProbationStatus(), updatedCase.getCaseNo()));
    }

    void updateOtherProbationStatusForCrn(String crn, String probationStatus, String caseNo) {
        if (crn != null) {
            final var courtCases = courtCaseRepository.findOtherCurrentCasesByCrn(crn, caseNo)
                .stream()
                .filter(courtCaseEntity -> !courtCaseEntity.getProbationStatus().equalsIgnoreCase(probationStatus))
                .map(courtCaseEntity -> CourtCaseMapper.create(courtCaseEntity, probationStatus))
                .collect(Collectors.toList());

            if (!courtCases.isEmpty()) {
                log.debug("Updating {} cases for CRN {} with changed probation status to {}", courtCases.size(), crn, probationStatus);
                courtCaseRepository.saveAll(courtCases);
            }
        }
    }

    void updateOtherProbationStatusForCrnByCaseId(String crn, String probationStatus, String caseId) {
        if (crn != null) {
            final var courtCases = courtCaseRepository.findOtherCurrentCasesByCrnNotCaseId(crn, caseId)
                .stream()
                .filter(courtCaseEntity -> !courtCaseEntity.getProbationStatus().equalsIgnoreCase(probationStatus))
                .map(courtCaseEntity -> CourtCaseMapper.create(courtCaseEntity, probationStatus))
                .collect(Collectors.toList());

            if (!courtCases.isEmpty()) {
                log.debug("Updating {} cases for CRN {} with changed probation status to {}", courtCases.size(), crn, probationStatus);
                courtCaseRepository.saveAll(courtCases);
            }
        }
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
    public List<CourtCaseEntity> filterCases(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore) {
        final var court = courtRepository.findByCourtCode(courtCode)
            .orElseThrow(() -> new EntityNotFoundException("Court %s not found", courtCode));

        if (caseListExtended) {
            return courtCaseRepository.findByCourtCodeAndHearingDay(court.getCourtCode(), hearingDay, createdAfter, createdBefore);
        }
        else {
            final var start = LocalDateTime.of(hearingDay, LocalTime.MIDNIGHT);
            return courtCaseRepository.findByCourtCodeAndSessionStartTime(court.getCourtCode(), start, start.plusDays(1), createdAfter, createdBefore);
        }
    }

    private void validateEntity(String caseId, CourtCaseEntity updatedCase) {
        checkCourtExists(updatedCase.getCourtCode());
        checkEntityCaseIdAgree(caseId, updatedCase);
    }

    private void checkEntityCaseIdAgree(String caseId, CourtCaseEntity updatedCase) {
        if (!caseId.equals(updatedCase.getCaseId())) {
            throw new ConflictingInputException(String.format("Case Id %s does not match with value from body %s",
                caseId, updatedCase.getCaseId()));
        }
    }

    private void validateEntity(String courtCode, String caseNo, CourtCaseEntity updatedCase) {
        checkCourtExists(updatedCase.getCourtCode());
        checkEntityCaseNoAndCourtAgree(courtCode, caseNo, updatedCase);
    }

    private void checkEntityCaseNoAndCourtAgree(String courtCode, String caseNo, CourtCaseEntity updatedCase) {
        if (!caseNo.equals(updatedCase.getCaseNo()) || !courtCode.equals(updatedCase.getCourtCode())) {
            throw new ConflictingInputException(String.format("Case No %s and Court Code %s do not match with values from body %s and %s",
                    caseNo, courtCode, updatedCase.getCaseNo(), updatedCase.getCourtCode()));
        }
    }

    private void checkCourtExists(String courtCode) throws EntityNotFoundException {
        courtRepository.findByCourtCode(courtCode)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Court %s not found", courtCode)));
    }

    private void updateOffenderMatches(CourtCaseEntity existingCase, CourtCaseEntity updatedCase) {
        final var groupedMatches = matchRepository.findByCourtCodeAndCaseNo(updatedCase.getCourtCode(), updatedCase.getCaseNo());
        groupedMatches
                .map(GroupedOffenderMatchesEntity::getOffenderMatches)
                .ifPresent(matches -> matches
                        .forEach(match -> confirmAndRejectMatches(existingCase, updatedCase, match)));
        groupedMatches.ifPresent(matchRepository::save);
    }

    private void confirmAndRejectMatches(CourtCaseEntity existingCase, CourtCaseEntity updatedCase, OffenderMatchEntity match) {
        boolean crnMatches = match.getCrn().equals(updatedCase.getCrn());
        match.setConfirmed(crnMatches);
        match.setRejected(!crnMatches);
        if (crnMatches){
            telemetryService.trackMatchEvent(TelemetryEventType.MATCH_CONFIRMED, match, updatedCase);
        } else {
            telemetryService.trackMatchEvent(TelemetryEventType.MATCH_REJECTED, match, updatedCase);
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
        final var start = LocalDateTime.of(searchDate, LocalTime.MIDNIGHT);
        return courtCaseRepository.findLastModified(courtCode, start, start.plusDays(1));
    }

    public Optional<LocalDateTime> findLastModifiedByHearingDay(String courtCode, LocalDate searchDate) {
        return courtCaseRepository.findLastModifiedByHearingDay(courtCode, searchDate);
    }

}
