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
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CourtCaseMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class ImmutableCourtCaseService implements CourtCaseService {

    private final CourtRepository courtRepository;
    private final CourtCaseRepository courtCaseRepository;
    private final HearingRepository hearingRepository;
    private final TelemetryService telemetryService;
    private final GroupedOffenderMatchRepository matchRepository;
    private final OffenderRepository offenderRepository;

    @Autowired
    public ImmutableCourtCaseService(CourtRepository courtRepository,
                                     CourtCaseRepository courtCaseRepository, HearingRepository hearingRepository,
                                     TelemetryService telemetryService,
                                     GroupedOffenderMatchRepository matchRepository,
                                     OffenderRepository offenderRepository) {
        this.courtRepository = courtRepository;
        this.courtCaseRepository = courtCaseRepository;
        this.hearingRepository = hearingRepository;
        this.telemetryService = telemetryService;
        this.matchRepository = matchRepository;
        this.offenderRepository = offenderRepository;
    }

    @Override
    @Retryable(value = CannotAcquireLockException.class)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Mono<HearingEntity> createHearing(String caseId, HearingEntity updatedHearing) throws EntityNotFoundException, InputMismatchException {
        validateEntity(caseId, updatedHearing);

        updateOffenders(updatedHearing, defendantEntity -> true);
        hearingRepository.findFirstByHearingIdOrderByIdDesc(Optional.ofNullable(updatedHearing.getHearingId()).orElse(caseId))
                .ifPresentOrElse(
                        existingHearing -> {
                            updatedHearing.getDefendants()
                                    .forEach(defendantEntity -> updateOffenderMatches(existingHearing, updatedHearing, defendantEntity.getDefendantId()));
                            trackUpdateEvents(existingHearing, updatedHearing);
                        },
                        () -> trackCreateEvents(updatedHearing));

        return Mono.just(updatedHearing)
                .map(hearingEntity -> {
                    log.debug("Saving case ID {}", caseId);
                    enforceValidHearingId(hearingEntity);
                    courtCaseRepository.save(hearingEntity.getCourtCase());
                    return hearingRepository.save(hearingEntity);
                });
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

    @Override
    @Retryable(value = CannotAcquireLockException.class)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Mono<HearingEntity> createUpdateHearingForSingleDefendantId(String caseId, String defendantId, HearingEntity updatedCase)
            throws EntityNotFoundException, InputMismatchException {
        validateEntityByDefendantId(caseId, defendantId, updatedCase);

        updateOffenders(updatedCase, (defendantEntity) -> defendantEntity.getDefendantId().equalsIgnoreCase(defendantId));

        // The case to be saved might change from the one passed in, through the addition of defendants
        var caseToSave = hearingRepository.findByCaseIdAndDefendantId(caseId, defendantId)
                .map((existingCase) -> {
                    // Ned to update matches, send some telemetry and copy the defendants on the existing case to this one
                    updateOffenderMatches(existingCase, updatedCase, defendantId);
                    trackUpdateEvents(existingCase, updatedCase);
                    return CourtCaseMapper.mergeDefendantsOnCase(existingCase, updatedCase, defendantId);
                })
                .orElseGet(() -> {
                    trackCreateEvents(updatedCase);
                    return updatedCase;
                });

        return Mono.just(caseToSave)
                .map((hearingEntity) -> {
                    log.debug("Saving case ID {} with updates applied for defendant ID {}", caseId, defendantId);
                    // TODO: Remove. This is a temporary measure to allow the application to continue working whilst we update the data structures
                    enforceValidHearingId(hearingEntity);
                    courtCaseRepository.save(hearingEntity.getCourtCase());
                    return hearingRepository.save(hearingEntity);
                });
    }

    void updateOffenders(HearingEntity updatedCourtCase, Predicate<DefendantEntity> defendantPredicate) {
        Optional.ofNullable(updatedCourtCase.getDefendants()).orElse(Collections.emptyList())
                .stream()
                .filter(defendantPredicate)
                .map(DefendantEntity::getOffender)
                .filter(Objects::nonNull)
                .forEach(updatedOffender -> {
                    final var existingOffender = offenderRepository.findByCrn(updatedOffender.getCrn());
                    existingOffender.ifPresentOrElse(offender -> {
                                updatedOffender.setId(offender.getId());
                                offender.setProbationStatus(updatedOffender.getProbationStatus());
                                offender.setAwaitingPsr(updatedOffender.getAwaitingPsr());
                                offender.setBreach(updatedOffender.isBreach());
                                offender.setPreSentenceActivity(updatedOffender.isPreSentenceActivity());
                                offender.setSuspendedSentenceOrder(updatedOffender.isSuspendedSentenceOrder());
                                offender.setPreviouslyKnownTerminationDate(updatedOffender.getPreviouslyKnownTerminationDate());
                                offenderRepository.save(offender);
                            },
                            () -> offenderRepository.save(updatedOffender));
                });
    }

    private void trackCreateEvents(HearingEntity createdCase) {
        telemetryService.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, createdCase);
        Optional.ofNullable(createdCase.getDefendants()).orElse(Collections.emptyList()).forEach((defendantEntity -> {
            if (defendantEntity.getOffender() != null) {
                telemetryService.trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, defendantEntity, createdCase.getCaseId());
            }
        }));
    }

    private void trackUpdateEvents(HearingEntity existingCase, HearingEntity updatedCase) {
        telemetryService.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCase);
        Optional.ofNullable(updatedCase.getDefendants()).orElse(Collections.emptyList()).forEach(defendantEntity -> {
            trackUpdateDefendantEvents(existingCase, defendantEntity, updatedCase.getCaseId());
        });
    }

    private void trackUpdateDefendantEvents(HearingEntity existingCase, DefendantEntity defendant, String caseId) {
        final var existingDefendant = existingCase.getDefendant(defendant.getDefendantId());
        final var wasLinked = Optional.ofNullable(existingDefendant).map(def -> def.getOffender() != null).orElse(false);
        final var isLinked = defendant.getOffender() != null;
        if (!wasLinked && isLinked)
            telemetryService.trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, defendant, caseId);
        else if (wasLinked && !isLinked)
            telemetryService.trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_UNLINKED, existingDefendant, caseId);
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
    public HearingEntity getHearingByCaseIdAndDefendantId(String caseId, String defendantId) throws EntityNotFoundException {
        log.info("Court case requested for case ID {} and defendant ID {}", caseId, defendantId);
        return hearingRepository.findByCaseIdAndDefendantId(caseId, defendantId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found for defendant %s", caseId, defendantId)));
    }

    @Override
    public List<HearingEntity> filterHearings(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore) {
        final var court = courtRepository.findByCourtCode(courtCode)
                .orElseThrow(() -> new EntityNotFoundException("Court %s not found", courtCode));

        return hearingRepository.findByCourtCodeAndHearingDay(court.getCourtCode(), hearingDay, createdAfter, createdBefore);
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

        checkEntityDefendantIdExists(defendantId, updatedCase);
    }

    private void checkEntityCaseIdAgree(String caseId, HearingEntity updatedCase) {
        if (!caseId.equals(updatedCase.getCaseId())) {
            throw new ConflictingInputException(String.format("Case Id %s does not match with value from body %s",
                    caseId, updatedCase.getCaseId()));
        }
    }

    private void checkEntityDefendantIdExists(String defendantId, HearingEntity updatedCase) {
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
        var defendant = updatedCase.getDefendant(defendantId);
        var offender = Optional.ofNullable(defendant).map(DefendantEntity::getOffender);
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
                    Optional.ofNullable(existingCase.getDefendants()).map(defendantEntities -> defendantEntities.stream().map(DefendantEntity::getDefendantId).collect(Collectors.toList())).orElse(null));
        }
        if (crnMatches && defendant.getCro() != null && !defendant.getCro().equals(match.getCro())) {
            log.warn("Unexpected CRO mismatch when updating offender match - matchId: {}, defendant ID: {}, matchCro: {}, updateCro: {}",
                    match.getId(), defendantId, match.getCro(),
                    Optional.ofNullable(existingCase.getDefendants()).map(defendantEntities -> defendantEntities.stream().map(DefendantEntity::getCro).collect(Collectors.toList())).orElse(null)
            );
        }
    }

    public Optional<LocalDateTime> filterHearingsLastModified(String courtCode, LocalDate searchDate) {
        return hearingRepository.findLastModifiedByHearingDay(courtCode, searchDate);
    }

}
