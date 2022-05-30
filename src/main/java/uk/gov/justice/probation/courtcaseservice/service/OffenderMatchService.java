package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.RequestScope;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetail;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetailResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.mapper.OffenderMatchMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequestScope
public class OffenderMatchService {

    private final GroupedOffenderMatchRepository groupedOffenderMatchRepository;
    private final OffenderRestClient offenderRestClient;
    private final CourtCaseRepository courtCaseRepository;
    private final HearingRepository hearingRepository;

    @Autowired
    public OffenderMatchService(GroupedOffenderMatchRepository groupedOffenderMatchRepository, OffenderRestClientFactory offenderRestClientFactory, CourtCaseRepository courtCaseRepository, HearingRepository hearingRepository) {
        this.groupedOffenderMatchRepository = groupedOffenderMatchRepository;
        this.offenderRestClient = offenderRestClientFactory.build();
        this.courtCaseRepository = courtCaseRepository;
        this.hearingRepository = hearingRepository;
    }

    @Deprecated(forRemoval = true)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(value = {CannotAcquireLockException.class, DataIntegrityViolationException.class})
    public Mono<GroupedOffenderMatchesEntity> createOrUpdateGroupedMatchesByDefendant(String caseId, String defendantId, GroupedOffenderMatchesRequest offenderMatches) {
        return Mono.just(groupedOffenderMatchRepository.findByCaseIdAndDefendantId(caseId, defendantId)
                        .map(existingGroup -> OffenderMatchMapper.update(caseId, defendantId, existingGroup, offenderMatches))
                        .orElseGet(() -> createForCaseAndDefendant(caseId, defendantId, offenderMatches)))
                .map(groupedOffenderMatchesEntity -> groupedOffenderMatchRepository.save(groupedOffenderMatchesEntity));
    }

    private GroupedOffenderMatchesEntity createForCaseAndDefendant(String caseId, String defendantId, GroupedOffenderMatchesRequest offenderMatches) {
        final var courtCaseEntity = courtCaseRepository.findFirstByCaseIdOrderByIdDesc(caseId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found", caseId)));
        return OffenderMatchMapper.newGroupedMatchesOf(defendantId, offenderMatches, courtCaseEntity);
    }

    @Deprecated(forRemoval = true)
    public Mono<GroupedOffenderMatchesEntity> getGroupedMatchesByCaseId(String caseId, String defendantId, Long groupId) {
        return Mono.justOrEmpty(groupedOffenderMatchRepository.findById(groupId))
                .map(groupedOffenderMatchesEntity -> {
                    if (!caseId.equals(groupedOffenderMatchesEntity.getCaseId())) {
                        throw new EntityNotFoundException(String.format("Grouped Matches %s not found for caseId %s", groupId, caseId));
                    }
                    return groupedOffenderMatchesEntity;
                });
    }

    @Deprecated(forRemoval = true)
    public OffenderMatchDetailResponse getOffenderMatchDetailsByCaseIdAndDefendantId(String caseId, String defendantId) {
        List<OffenderMatchDetail> offenderMatchDetails = getOffenderMatchesByCaseIdAndDefendantId(caseId, defendantId)
                .map(GroupedOffenderMatchesEntity::getOffenderMatches)
                .map(offenderMatchEntities -> offenderMatchEntities
                        .stream()
                        .map(OffenderMatchEntity::getCrn)
                        .map(this::getOffenderMatchDetail)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                ).orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found for defendant %s", caseId, defendantId)));

        return OffenderMatchDetailResponse.builder().offenderMatchDetails(offenderMatchDetails).build();
    }

    public List<OffenderMatchDetail> getOffenderMatchDetailsByDefendantId(String defendantId) {

        return groupedOffenderMatchRepository.findFirstByDefendantIdOrderByIdDesc(defendantId)
            .map(GroupedOffenderMatchesEntity::getOffenderMatches)
            .map(offenderMatchEntities -> offenderMatchEntities
                .stream()
                .map(OffenderMatchEntity::getCrn)
                .map(this::getOffenderMatchDetail)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
            ).orElseThrow(() -> new EntityNotFoundException(String.format("Defendant %s not found", defendantId)));
    }

    private Optional<GroupedOffenderMatchesEntity> getOffenderMatchesByCaseIdAndDefendantId(String caseId, String defendantId) {
        return groupedOffenderMatchRepository.findByCaseIdAndDefendantId(caseId, defendantId);
    }

    public Optional<Integer> getMatchCountByCaseIdAndDefendant(String caseId, String defendantId) {
        return groupedOffenderMatchRepository.getMatchCountByCaseIdAndDefendant(caseId, defendantId);
    }

    OffenderMatchDetail getOffenderMatchDetail(String crn) {
        log.debug("Looking for offender detail for CRN :{}", crn);
        return Mono.zip(offenderRestClient.getOffenderMatchDetailByCrn(crn),
                        offenderRestClient.getConvictionsByCrn(crn)
                                .onErrorResume(OffenderNotFoundException.class, e -> Mono.just(Collections.emptyList())),
                        offenderRestClient.getProbationStatusByCrn(crn))
                .map(tuple -> addMostRecentEventToOffenderMatch(tuple.getT1(), tuple.getT2(), tuple.getT3()))
                .block();
    }

    private OffenderMatchDetail addMostRecentEventToOffenderMatch(OffenderMatchDetail offenderMatchDetail,
                                                                  List<Conviction> convictions,
                                                                  ProbationStatusDetail probationStatus) {

        if (offenderMatchDetail == null) {
            return null;
        }

        Sentence sentence = Optional.ofNullable(convictions).orElse(Collections.emptyList()).stream()
                .filter(Conviction::getActive)
                .findFirst()
                .map(Conviction::getSentence)
                .orElse(getSentenceForMostRecentConviction(convictions));

        return OffenderMapper.offenderMatchDetailFrom(offenderMatchDetail, sentence, probationStatus);
    }

    Sentence getSentenceForMostRecentConviction(List<Conviction> convictions) {
        return Optional.ofNullable(convictions).orElse(Collections.emptyList()).stream()
                .min(Comparator.comparing(Conviction::getConvictionDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(Conviction::getSentence)
                .orElse(null);
    }

    public Mono<GroupedOffenderMatchesEntity> getGroupedOffenderMatchesByDefendantIdAndGroupId(String defendantId, Long groupId) {
        return Mono.justOrEmpty(groupedOffenderMatchRepository.findById(groupId))
                .map(groupedOffenderMatchesEntity -> {
                    if (!defendantId.equals(groupedOffenderMatchesEntity.getDefendantId())) {
                        throw new EntityNotFoundException(String.format("Grouped Matches %s not found for defendant %s", groupId, defendantId));
                    }
                    return groupedOffenderMatchesEntity;
                });
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(value = {CannotAcquireLockException.class, DataIntegrityViolationException.class})
    public Mono<GroupedOffenderMatchesEntity> createOrUpdateGroupedMatchesByDefendant(String defendantId, GroupedOffenderMatchesRequest groupedOffenderMatchesRequest) {
        return Mono.justOrEmpty(hearingRepository.findFirstByHearingDefendantsDefendantId(defendantId))
                .map(hearingEntity -> {
                    if (hearingEntity != null) {
                        return createOrUpdateGroupedMatchesByDefendant(hearingEntity, defendantId, groupedOffenderMatchesRequest);
                    }
                    throw new EntityNotFoundException(String.format("Hearing  entity not found for defendant %s", defendantId));
                })
                .map(groupedOffenderMatchRepository::save);

    }

    private GroupedOffenderMatchesEntity createOrUpdateGroupedMatchesByDefendant(HearingEntity hearingEntity, String defendantId, GroupedOffenderMatchesRequest offenderMatches) {
        String caseId = hearingEntity.getCaseId();
        return groupedOffenderMatchRepository.findByCaseIdAndDefendantId(caseId, defendantId)
                .map(existingGroup -> OffenderMatchMapper.update(caseId, defendantId, existingGroup, offenderMatches))
                .orElseGet(() -> OffenderMatchMapper.newGroupedMatchesOf(defendantId, offenderMatches, caseId));
    }
}
