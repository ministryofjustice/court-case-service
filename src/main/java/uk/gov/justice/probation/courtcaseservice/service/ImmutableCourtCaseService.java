package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.controller.mapper.CourtCaseResponseMapper;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseListFilters;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseListResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseSearchFilter;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEventType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CaseSearchRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.justice.probation.courtcaseservice.service.predicate.CaseSearchFilterPredicate.applyFilters;
import static uk.gov.justice.probation.courtcaseservice.service.specification.CaseSearchSpecification.getCaseSearchSpecification;

@Service
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class ImmutableCourtCaseService implements CourtCaseService {

    private final CourtRepository courtRepository;
    private final HearingRepositoryFacade hearingRepositoryFacade;
    private final TelemetryService telemetryService;
    private final GroupedOffenderMatchRepository matchRepository;
    private final DomainEventService domainEventService;
    private final CourtCaseRepository courtCaseRepository;

    private final CaseSearchRepository caseSearchRepository;

    @Autowired
    public ImmutableCourtCaseService(CourtRepository courtRepository,
                                     HearingRepositoryFacade hearingRepositoryFacade,
                                     TelemetryService telemetryService,
                                     GroupedOffenderMatchRepository matchRepository,
                                     DomainEventService domainEventService,
                                     CourtCaseRepository courtCaseRepository,
                                     CaseSearchRepository caseSearchRepository) {
        this.courtRepository = courtRepository;
        this.hearingRepositoryFacade = hearingRepositoryFacade;
        this.telemetryService = telemetryService;
        this.matchRepository = matchRepository;
        this.domainEventService = domainEventService;
        this.courtCaseRepository = courtCaseRepository;
        this.caseSearchRepository = caseSearchRepository;
    }

    @Override
    @Retryable(value = CannotAcquireLockException.class)
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Deprecated(forRemoval = true)
    public Mono<HearingEntity> createHearing(String caseId, HearingEntity updatedHearing) throws EntityNotFoundException, InputMismatchException {
        validateEntity(caseId, updatedHearing);

        return createOrUpdateHearing(Optional.ofNullable(updatedHearing.getHearingId()).orElse(caseId), updatedHearing);
    }

    @Override
    @Retryable(value = CannotAcquireLockException.class)
    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRES_NEW)
    public Mono<HearingEntity> createOrUpdateHearingByHearingId(String hearingId, HearingEntity updatedHearing) throws EntityNotFoundException, InputMismatchException {
        validateCourtCode(updatedHearing);
        if (!StringUtils.equals(hearingId, updatedHearing.getHearingId())) {
            throw new ConflictingInputException(String.format("Hearing Id %s does not match with value from body %s",
                    hearingId, updatedHearing.getHearingId()));
        }

        Mono<HearingEntity> orUpdateHearing = createOrUpdateHearing(hearingId, updatedHearing);
        return orUpdateHearing;
    }

    @Override
    public HearingEntity getHearingByCaseNumber(String courtCode, String caseNo, String listNo) throws EntityNotFoundException {
        checkCourtExists(courtCode);
        log.info("Court case requested for court {} for case {}", courtCode, caseNo);
        return hearingRepositoryFacade.findByCourtCodeAndCaseNo(courtCode, caseNo, listNo)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found for court %s", caseNo, courtCode)));
    }

    @Override
    public HearingEntity getHearingByHearingId(String hearingId) throws EntityNotFoundException {
        log.info("Court case requested for hearing ID {}", hearingId);
        return hearingRepositoryFacade.findFirstByHearingId(hearingId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Hearing %s not found", hearingId)));
    }

    @Override
    public HearingEntity getHearingByHearingIdAndDefendantId(String hearingId, String defendantId) throws EntityNotFoundException {
        log.info("Court case requested for hearing ID {} and defendant ID {}", hearingId, defendantId);
        return hearingRepositoryFacade.findByHearingIdAndDefendantId(hearingId, defendantId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Hearing %s not found for defendant %s", hearingId, defendantId)));
    }

    @Override
    public List<HearingEntity> filterHearings(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore) {
        final var court = courtRepository.findByCourtCode(courtCode)
                .orElseThrow(() -> new EntityNotFoundException("Court %s not found", courtCode));

        return hearingRepositoryFacade.findByCourtCodeAndHearingDay(court.getCourtCode(), hearingDay, createdAfter, createdBefore);
    }

    public Optional<LocalDateTime> filterHearingsLastModified(String courtCode, LocalDate searchDate) {
        return hearingRepositoryFacade.findLastModifiedByHearingDay(courtCode, searchDate);
    }

    @Override
    public CaseListResponse findCourtCases(CaseSearchFilter caseSearchFilter, Pageable pageable) {
        Specification<HearingEntity> searchSpecification = getCaseSearchSpecification(caseSearchFilter);

        var searchResults = caseSearchRepository.findAll(searchSpecification);

        if(isNotEmpty(searchResults)) {

            return getCaseListResponse(caseSearchFilter, pageable, searchResults);
        }
        // empty response with filters
        return CaseListResponse.builder()
                .cases(Collections.emptyList())
                .filters(CaseListFilters.builder().build())
                .build();
    }

    private CaseListResponse getCaseListResponse(CaseSearchFilter caseSearchFilter, Pageable pageable, List<HearingEntity> searchResults) {
        var possibleNDeliusCount = (int) searchResults.stream()
                .map(this::countPossibleNDeliusRecords).count();

        var recentlyAddedCount = countRecentlyAdded(searchResults);

        var courtRooms = hearingRepositoryFacade.getDistinctCourtRoom(caseSearchFilter.getCourtCode());

        var totalNoOfPages = searchResults.size() < pageable.getPageSize() ? 1 : (int) Math.ceil((double) searchResults.size() / pageable.getPageSize());

        var caseListFilter = CaseListFilters.builder()
                .possibleNdeliusRecords(possibleNDeliusCount)
                .recentlyAdded(recentlyAddedCount)
                .size(pageable.getPageSize())
                .courtRoom(courtRooms)
                .totalNoOfPages(totalNoOfPages)
                .build();

        var filteredResults = applyFilters(searchResults, caseSearchFilter);

        var caseLists = filteredResults.stream()
                .flatMap(courtCaseEntity -> buildCourtCaseResponses(courtCaseEntity, caseSearchFilter.getDate()).stream())
                .sorted(Comparator
                        .comparing(CourtCaseResponse::getCourtRoom)
                        .thenComparing(CourtCaseResponse::getSessionStartTime)
                        .thenComparing(CourtCaseResponse::getName)).toList();


        var pageItems = getPageItems(caseLists, pageable.getPageNumber(), pageable.getPageSize());

        return CaseListResponse.builder()
                .cases(pageItems)
                .filters(caseListFilter)
                .build();
    }

    private List<CourtCaseResponse> getPageItems(List<CourtCaseResponse> availableCourtCaseServiceList, int pageNumber, int pageSize) {
        int skipCount = (pageNumber - 1) * pageSize;

        return availableCourtCaseServiceList
                .stream()
                .skip(skipCount)
                .limit(pageSize).toList();

    }

    private int countPossibleNDeliusRecords(HearingEntity hearingEntity) {

        var defendantEntities = new ArrayList<>(Optional.ofNullable(hearingEntity.getHearingDefendants()).orElse(Collections.emptyList()));
        final var caseId = hearingEntity.getCaseId();
        return (int) defendantEntities.stream()
                .map(hearingDefendantEntity -> {
                    final String defendantId = Optional.of(hearingDefendantEntity).map(HearingDefendantEntity::getDefendant).map(DefendantEntity::getDefendantId).orElseThrow();
                    return matchRepository.getMatchCountByCaseIdAndDefendant(caseId, defendantId).orElse(0);
                }).count();
    }

    private int countRecentlyAdded(List<HearingEntity> hearingEntities) {
        return (int) hearingEntities.stream()
                .map(hearingEntity -> {
                    return LocalDate.now().isEqual(Optional.ofNullable(hearingEntity.getFirstCreated()).orElse(LocalDateTime.now()).toLocalDate());
                }).count();
    }


    private List<CourtCaseResponse> buildCourtCaseResponses(HearingEntity hearingEntity, LocalDate hearingDate) {

        var defendantEntities = new ArrayList<>(Optional.ofNullable(hearingEntity.getHearingDefendants()).orElse(Collections.emptyList()));

        final var caseId = hearingEntity.getCaseId();
        return defendantEntities.stream()
                .sorted(Comparator.comparing(HearingDefendantEntity::getDefendantSurname))
                .map(hearingDefendantEntity -> {
                    return CourtCaseResponseMapper.mapFrom(hearingEntity, hearingDefendantEntity, hearingDate);
                })
                .toList();
    }

    private Mono<HearingEntity> createOrUpdateHearing(String hearingId, final HearingEntity updatedHearing) {
        var hearing = hearingRepositoryFacade.findFirstByHearingId(hearingId)
                .map(existingHearing -> {
                    trackUpdateEvents(existingHearing, updatedHearing);
                    return existingHearing.update(updatedHearing);
                })
                .orElseGet(() -> {
                    trackCreateEvents(updatedHearing);
                    courtCaseRepository.findFirstByCaseIdOrderByIdDesc(updatedHearing.getCaseId())
                            .ifPresent(courtCaseEntity -> {
                                addHearingToCase(updatedHearing, courtCaseEntity);
                            });
                    return updatedHearing;
                });
        log.debug("Saving hearing with ID {}", hearingId);

        var savedHearing = hearingRepositoryFacade.save(hearing);
        return Mono.just(savedHearing)
                .map(saved -> {
                    if (hasSentencedEventType(saved)) {
                        log.debug("Emitting sentenced event for hearing with ID {}", hearingId);
                        domainEventService.emitSentencedEvent(saved);
                    }
                    return saved;
                });
    }

    private static void addHearingToCase(HearingEntity updatedHearing, CourtCaseEntity courtCaseEntity) {
        courtCaseEntity.addHearing(updatedHearing);
    }

    private boolean hasSentencedEventType(HearingEntity hearingEntity) {
        return hearingEntity.getHearingEventType() != null && hearingEntity.getHearingEventType().equals(HearingEventType.RESULTED);
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
        validateCourtCode(updatedCase);
        checkEntityCaseIdAgree(caseId, updatedCase);
    }

    private void validateCourtCode(HearingEntity updatedCase) {
        updatedCase.getHearingDays()
                .stream()
                .map(HearingDayEntity::getCourtCode)
                .forEach(courtCode -> checkCourtExists(courtCode, true));
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
