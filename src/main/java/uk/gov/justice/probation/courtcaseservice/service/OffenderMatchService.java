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
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.mapper.OffenderMatchMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequestScope
public class OffenderMatchService {
    private CourtCaseService courtCaseService;
    private GroupedOffenderMatchRepository offenderMatchRepository;
    private OffenderRestClient offenderRestClient;

    @Autowired
    public OffenderMatchService(CourtCaseService courtCaseService, GroupedOffenderMatchRepository offenderMatchRepository, OffenderRestClientFactory offenderRestClientFactory) {
        this.courtCaseService = courtCaseService;
        this.offenderMatchRepository = offenderMatchRepository;
        this.offenderRestClient = offenderRestClientFactory.build();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(value = {CannotAcquireLockException.class, DataIntegrityViolationException.class})
    public Mono<GroupedOffenderMatchesEntity> createOrUpdateGroupedMatchesByDefendant(String caseId, String defendantId, GroupedOffenderMatchesRequest offenderMatches) {
        return Mono.just(offenderMatchRepository.findByCaseIdAndDefendantId(caseId, defendantId)
            .map(existingGroup -> OffenderMatchMapper.update(caseId, defendantId, existingGroup, offenderMatches))
            .orElseGet(() -> createForCaseAndDefendant(caseId, defendantId, offenderMatches)))
            .map(groupedOffenderMatchesEntity -> offenderMatchRepository.save(groupedOffenderMatchesEntity));
    }

    private GroupedOffenderMatchesEntity createForCaseAndDefendant(String caseId, String defendantId, GroupedOffenderMatchesRequest offenderMatches) {
        final var courtCaseEntity = courtCaseService.getHearingByCaseId(caseId);
        return OffenderMatchMapper.newGroupedMatchesOf(defendantId, offenderMatches, courtCaseEntity);
    }

    public Mono<GroupedOffenderMatchesEntity> getGroupedMatchesByCaseId(String caseId, String defendantId, Long groupId) {
        return Mono.justOrEmpty(offenderMatchRepository.findById(groupId))
            .map(groupedOffenderMatchesEntity -> {
                if (!caseId.equals(groupedOffenderMatchesEntity.getCaseId())) {
                    throw new EntityNotFoundException(String.format("Grouped Matches %s not found for caseId %s", groupId, caseId));
                }
                return groupedOffenderMatchesEntity;
            });
    }

    public OffenderMatchDetailResponse getOffenderMatchDetailsByCaseIdAndDefendantId(String caseId, String defendantId) {
        courtCaseService.getHearingByCaseIdAndDefendantId(caseId, defendantId);    // Throw EntityNotFound if case does not exist
        List<OffenderMatchDetail> offenderMatchDetails = getOffenderMatchesByCaseIdAndDefendantId(caseId, defendantId)
            .map(GroupedOffenderMatchesEntity::getOffenderMatches)
            .map(offenderMatchEntities -> offenderMatchEntities
                .stream()
                .map(OffenderMatchEntity::getCrn)
                .map(this::getOffenderMatchDetail)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
            ).orElse(Collections.emptyList());

        return OffenderMatchDetailResponse.builder().offenderMatchDetails(offenderMatchDetails).build();
    }

    private Optional<GroupedOffenderMatchesEntity> getOffenderMatchesByCaseIdAndDefendantId(String caseId, String defendantId) {
        return offenderMatchRepository.findByCaseIdAndDefendantId(caseId, defendantId);
    }

    public Optional<Integer> getMatchCountByCaseIdAndDefendant(String caseId, String defendantId) {
        return offenderMatchRepository.getMatchCountByCaseIdAndDefendant(caseId, defendantId);
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
}
