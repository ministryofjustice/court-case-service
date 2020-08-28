package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetail;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetailResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.mapper.OffenderMatchMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class OffenderMatchService {
    @Autowired
    private CourtCaseService courtCaseService;

    @Autowired
    private GroupedOffenderMatchRepository offenderMatchRepository;

    @Autowired
    private OffenderMatchMapper mapper;

    @Autowired
    private OffenderRestClient offenderRestClient;

    @Autowired
    private OffenderMapper offenderMapper;

    public Mono<GroupedOffenderMatchesEntity> createGroupedMatches(String courtCode, String caseNo, GroupedOffenderMatchesRequest offenderMatches) {
        return Mono.just(courtCaseService.getCaseByCaseNumber(courtCode, caseNo))
                .map(courtCase -> mapper.groupedMatchesOf(offenderMatches, courtCase))
                .map(matchesEntity -> offenderMatchRepository.save(matchesEntity));
    }

    public Mono<GroupedOffenderMatchesEntity> getGroupedMatches(String courtCode, String caseNo, Long groupId) {
        return Mono.justOrEmpty(offenderMatchRepository.findById(groupId))
                .map(e -> {
                    if (!caseNo.equals(e.getCourtCase().getCaseNo()) || !courtCode.equals(e.getCourtCase().getCourtCode())) {
                        throw new EntityNotFoundException(String.format("Grouped Matches %s not found for court %s and caseNo %s", groupId, courtCode, caseNo));
                    }
                    return e;
                });
    }

    public OffenderMatchDetailResponse getOffenderMatchDetails(String courtCode, String caseNo) {

        CourtCaseEntity courtCaseEntity = courtCaseService.getCaseByCaseNumber(courtCode, caseNo);
        List<OffenderMatchDetail> offenderMatchDetails = offenderMatchRepository.findByCourtCase(courtCaseEntity).stream()
            .flatMap(group -> group.getOffenderMatches().stream())
            .map(OffenderMatchEntity::getCrn)
            .map(this::getOffenderMatchDetail)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return OffenderMatchDetailResponse.builder().offenderMatchDetails(offenderMatchDetails).build();
    }

    OffenderMatchDetail getOffenderMatchDetail(String crn) {
        log.debug("Looking for offender detail for CRN :{}", crn);
        return Mono.zip(offenderRestClient.getOffenderMatchDetailByCrn(crn),
                        offenderRestClient.getConvictionsByCrn(crn)
                        .onErrorResume(OffenderNotFoundException.class, e -> Mono.just(Collections.emptyList())))
            .map(tuple -> addMostRecentEventToOffenderMatch(tuple.getT1(), tuple.getT2()))
            .block();
    }

    OffenderMatchDetail addMostRecentEventToOffenderMatch(OffenderMatchDetail offenderMatchDetail, List<Conviction> convictions) {

        if (offenderMatchDetail == null) {
            return null;
        }

        Sentence sentence = Optional.ofNullable(convictions).orElse(Collections.emptyList()).stream()
            .filter(Conviction::getActive)
            .findFirst()
            .map(Conviction::getSentence)
            .orElse(getSentenceForMostRecentConviction(convictions));

        return offenderMapper.offenderMatchDetailFrom(offenderMatchDetail, sentence);
    }

    Sentence getSentenceForMostRecentConviction(List<Conviction> convictions) {
        return Optional.ofNullable(convictions).orElse(Collections.emptyList()).stream()
            .min(Comparator.comparing(Conviction::getConvictionDate, Comparator.nullsLast(Comparator.reverseOrder())))
            .map(Conviction::getSentence)
            .orElse(null);
    }
}
