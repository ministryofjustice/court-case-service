package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CourtCaseMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class ImmutableCourtCaseService implements CourtCaseService {

    private final CourtRepository courtRepository;
    private final HearingRepositoryFacade hearingRepository;
    private final TelemetryService telemetryService;
    private final GroupedOffenderMatchRepository matchRepository;

    @Autowired
    public ImmutableCourtCaseService(CourtRepository courtRepository,
                                     HearingRepositoryFacade hearingRepository,
                                     TelemetryService telemetryService,
                                     GroupedOffenderMatchRepository matchRepository) {
        this.courtRepository = courtRepository;
        this.hearingRepository = hearingRepository;
        this.telemetryService = telemetryService;
        this.matchRepository = matchRepository;
    }

    @Override
    @Retryable(value = CannotAcquireLockException.class)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Mono<HearingEntity> createHearing(String caseId, HearingEntity updatedHearing) throws EntityNotFoundException, InputMismatchException {
        validateEntity(caseId, updatedHearing);

        hearingRepository.findFirstByHearingIdOrderByIdDesc(Optional.ofNullable(updatedHearing.getHearingId()).orElse(caseId))
                .ifPresentOrElse(
                        existingHearing -> {
                            updatedHearing.getHearingDefendants()
                                    .stream().map(HearingDefendantEntity::getDefendant)
                                    .forEach(defendantEntity -> updateOffenderMatches(existingHearing, updatedHearing, defendantEntity.getDefendantId()));
                            trackUpdateEvents(existingHearing, updatedHearing);
                        },
                        () -> trackCreateEvents(updatedHearing));

        return Mono.just(updatedHearing)
                .map(hearingEntity -> {
                    log.debug("Saving case ID {}", caseId);
                    enforceValidHearingId(hearingEntity);
                    return hearingRepository.save(hearingEntity);
                });
    }

    @Override
    @Retryable(value = CannotAcquireLockException.class)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Mono<HearingEntity> createUpdateHearingForSingleDefendantId(String hearingId, String defendantId, HearingEntity updatedCase)
            throws EntityNotFoundException, InputMismatchException {
        validateEntityByDefendantId(hearingId, defendantId, updatedCase);

        // The case to be saved might change from the one passed in, through the addition of defendants
        var caseToSave = hearingRepository.findByCaseIdAndDefendantId(hearingId, defendantId)
                .map((existingCase) -> {
                    // Ned to update matches, send some telemetry and copy the defendants on the existing case to this one
                    updateOffenderMatches(existingCase, updatedCase, defendantId);
                    trackUpdateEvents(existingCase, updatedCase);
                    return CourtCaseMapper.mergeDefendantsOnHearing(existingCase, updatedCase, defendantId);
                })
                .orElseGet(() -> {
                    trackCreateEvents(updatedCase);
                    return updatedCase;
                });

        return Mono.just(caseToSave)
                .map((hearingEntity) -> {
                    log.debug("Saving case ID {} with updates applied for defendant ID {}", hearingId, defendantId);
                    // TODO: Remove. This is a temporary measure to allow the application to continue working whilst we update the data structures
                    enforceValidHearingId(hearingEntity);
                    return hearingRepository.save(hearingEntity);
                });
    }

    @Override
    public HearingEntity getHearingByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException {
        checkCourtExists(courtCode);
        log.info("Court case requested for court {} for case {}", courtCode, caseNo);
        return hearingRepository.findByCourtCodeAndCaseNo(courtCode, caseNo)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found for court %s", caseNo, courtCode)));
    }

    @Override
    public HearingEntity getHearingByCaseId(String caseId) throws EntityNotFoundException {
        log.info("Court case requested for case ID {}", caseId);
        return hearingRepository.findByCaseId(caseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found", caseId)));
    }

    @Override
    public HearingEntity getHearingByHearingId(String hearingId) throws EntityNotFoundException {
        log.info("Court case requested for hearing ID {}", hearingId);
        return hearingRepository.findFirstByHearingIdOrderByIdDesc(hearingId)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Hearing %s not found", hearingId)));
    }

    @Override
    public HearingEntity getHearingByCaseIdAndDefendantId(String hearingId, String defendantId) throws EntityNotFoundException {
        log.info("Court case requested for case ID {} and defendant ID {}", hearingId, defendantId);
        return hearingRepository.findByCaseIdAndDefendantId(hearingId, defendantId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found for defendant %s", hearingId, defendantId)));
    }

    @Override
    public HearingEntity getHearingByHearingIdAndDefendantId(String hearingId, String defendantId) throws EntityNotFoundException {
        log.info("Court case requested for hearing ID {} and defendant ID {}", hearingId, defendantId);
        return hearingRepository.findByHearingIdAndDefendantId(hearingId, defendantId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Hearing %s not found for defendant %s", hearingId, defendantId)));
    }

    @Override
    public List<HearingEntity> filterHearings(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore) {
        final var court = courtRepository.findByCourtCode(courtCode)
                .orElseThrow(() -> new EntityNotFoundException("Court %s not found", courtCode));

        return hearingRepository.findByCourtCodeAndHearingDay(court.getCourtCode(), hearingDay, createdAfter, createdBefore);
    }

    public Optional<LocalDateTime> filterHearingsLastModified(String courtCode, LocalDate searchDate) {
        return hearingRepository.findLastModifiedByHearingDay(courtCode, searchDate);
    }

    /**
     * This method was introduced to guarantee that hearingId is always set to match the caseId whilst data migration is
     * underway and flag where this is not the case so fixes can be implemented.  All being well the if condition will
     * never be met and this method will be removed in the next batch of changes
     * @param hearingEntity
     */
    @Deprecated(forRemoval = true)
    private void enforceValidHearingId(HearingEntity hearingEntity) {
        // TODO: Remove. This is a temporary measure to allow the application to continue working whilst we update the data structures
        if(hearingEntity.getHearingId() == null){
            log.warn("Unexpected condition: HearingEntity did not have hearingId set as expected. Setting to caseId {}", hearingEntity.getCaseId());
            hearingEntity.setHearingId(hearingEntity.getCaseId());
        }

    }

    private void trackCreateEvents(HearingEntity createdCase) {
        telemetryService.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, createdCase);
        Optional.ofNullable(createdCase.getHearingDefendants()).orElse(Collections.emptyList())
                .forEach((hearingDefendantEntity -> {
                    if (hearingDefendantEntity.getDefendant().getOffender() != null) {
                        telemetryService.trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, hearingDefendantEntity, createdCase.getCaseId());
                    }
        }));
    }

    private void trackUpdateEvents(HearingEntity existingCase, HearingEntity updatedCase) {
        telemetryService.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCase);
        Optional.ofNullable(updatedCase.getHearingDefendants()).orElse(Collections.emptyList()).forEach(defendantEntity -> {
            trackUpdateDefendantEvents(existingCase, defendantEntity, updatedCase.getCaseId());
        });
    }

    private void trackUpdateDefendantEvents(HearingEntity existingCase, HearingDefendantEntity defendant, String caseId) {
        final var existingDefendant = existingCase.getHearingDefendant(defendant.getDefendantId());
        final var wasLinked = Optional.ofNullable(existingDefendant)
                .map(HearingDefendantEntity::getDefendant)
                .map(def -> def.getOffender() != null).orElse(false);
        final var isLinked = Optional.ofNullable(defendant.getDefendant()).map(DefendantEntity::getOffender).orElse(null) != null;
        if (!wasLinked && isLinked)
            telemetryService.trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, defendant, caseId);
        else if (wasLinked && !isLinked)
            telemetryService.trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_UNLINKED, existingDefendant, caseId);
    }

    private void validateEntity(String caseId, HearingEntity updatedCase) {
        updatedCase.getHearingDays()
                .stream()
                .map(HearingDayEntity::getCourtCode)
                .forEach(courtCode -> checkCourtExists(courtCode, true));
        checkEntityCaseIdAgree(caseId, updatedCase);
    }

    private void validateEntityByDefendantId(String caseId, String defendantId, HearingEntity updatedCase) {
        validateEntity(caseId, updatedCase);

        checkEntityDefendantIdExists(updatedCase, defendantId);
    }

    private void checkEntityCaseIdAgree(String caseId, HearingEntity updatedCase) {
        if (!caseId.equals(updatedCase.getCaseId())) {
            throw new ConflictingInputException(String.format("Case Id %s does not match with value from body %s",
                    caseId, updatedCase.getCaseId()));
        }
    }

    private void checkEntityDefendantIdExists(HearingEntity updatedHearing, String defendantId) {
        var defendants = Optional.ofNullable(updatedHearing.getHearingDefendants()).orElse(Collections.emptyList());
        // Should not be possible because the updatedCase is built from the CourtCaseEntity with one defendant
        if (defendants.size() != 1) {
            throw new IllegalArgumentException(String.format("More than one defendant submitted on the CourtCaseEntity for single defendant update %s", defendantId));
        }
        final var entityDefendantId = defendants.get(0).getDefendantId();
        if (!defendantId.equalsIgnoreCase(entityDefendantId)) {
            throw new ConflictingInputException(String.format("Defendant Id '%s' does not match the one in the CourtCaseEntity body submitted '%s'",
                    defendantId, entityDefendantId));
        }
    }

    private void checkCourtExists(String courtCode) throws EntityNotFoundException {
        checkCourtExists(courtCode, false);
    }

    private void checkCourtExists(String courtCode, boolean createNotFound) throws EntityNotFoundException {
        if (courtRepository.findByCourtCode(courtCode).isPresent())
            return;

        if (createNotFound) {
            log.warn("Court code {} not found, saving as new Unknown Court.", courtCode);
            courtRepository.save(CourtEntity.builder()
                    .courtCode(courtCode)
                    .name("Unknown Court")
                    .build());
        } else {
            throw new EntityNotFoundException(String.format("Court %s not found", courtCode));
        }
    }

    private void updateOffenderMatches(HearingEntity existingCase, HearingEntity updatedCase, String defendantId) {
        final var groupedMatches = matchRepository.findByCaseIdAndDefendantId(updatedCase.getCaseId(), defendantId);
        groupedMatches
                .map(GroupedOffenderMatchesEntity::getOffenderMatches)
                .ifPresent(matches -> matches
                        .forEach(match -> confirmAndRejectMatches(existingCase, updatedCase, match, defendantId)));
        groupedMatches.ifPresent(matchRepository::save);
    }

    private void confirmAndRejectMatches(HearingEntity existingCase, HearingEntity updatedCase, OffenderMatchEntity match, String defendantId) {
        var defendant = updatedCase.getHearingDefendant(defendantId).getDefendant();
        var offender = Optional.ofNullable(defendant)
                .map(DefendantEntity::getOffender);
        boolean crnMatches = match.getCrn().equals(offender.map(OffenderEntity::getCrn).orElse(null));

        match.setConfirmed(crnMatches);
        match.setRejected(!crnMatches);
        if (crnMatches) {
            telemetryService.trackMatchEvent(TelemetryEventType.MATCH_CONFIRMED, match, updatedCase, defendantId);
        } else {
            telemetryService.trackMatchEvent(TelemetryEventType.MATCH_REJECTED, match, updatedCase, defendantId);
        }

        if (crnMatches && defendant.getPnc() != null && !defendant.getPnc().equals(match.getPnc())) {
            log.warn("Unexpected PNC mismatch when updating offender match - matchId: {}, defendant ID: {}, matchPnc: {}, updatePnc: {}",
                    match.getId(), defendantId, match.getPnc(),
                    Optional.ofNullable(existingCase.getHearingDefendants()).map(defendantEntities -> defendantEntities.stream().map(HearingDefendantEntity::getDefendantId).collect(Collectors.toList())).orElse(null));
        }
        if (crnMatches && defendant.getCro() != null && !defendant.getCro().equals(match.getCro())) {
            log.warn("Unexpected CRO mismatch when updating offender match - matchId: {}, defendant ID: {}, matchCro: {}, updateCro: {}",
                    match.getId(), defendantId, match.getCro(),
                    Optional.ofNullable(existingCase.getHearingDefendants())
                            .map(defendantEntities -> defendantEntities.stream()
                                .map(HearingDefendantEntity::getDefendant)
                                .map(DefendantEntity::getCro)
                                .collect(Collectors.toList())).orElse(null)
            );
        }
    }

}
