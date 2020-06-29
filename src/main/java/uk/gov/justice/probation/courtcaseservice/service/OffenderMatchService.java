package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.mapper.OffenderMatchMapper;

@Service
@AllArgsConstructor
public class OffenderMatchService {
    @Autowired
    private CourtCaseService courtCaseService;

    @Autowired
    private OffenderMatchRepository offenderMatchRepository;

    @Autowired
    private OffenderMatchMapper mapper;

    public Mono<GroupedOffenderMatchesEntity> createGroupedMatches(String courtCode, String caseNo, GroupedOffenderMatchesRequest offenderMatches) {
        return Mono.just(courtCaseService.getCaseByCaseNumber(courtCode, caseNo))
                .map(courtCase -> mapper.entityOf(offenderMatches, courtCase))
                .map(matchesEntity -> offenderMatchRepository.save(matchesEntity));
    }

    public Mono<GroupedOffenderMatchesEntity> getGroupedMatches(String courtCode, String caseNo, Long groupId) {
        return Mono.justOrEmpty(offenderMatchRepository.findById(groupId))
                .map(e -> {
                    if (!caseNo.equals(e.getCaseNo()) || !courtCode.equals(e.getCourtCode())) {
                        throw new EntityNotFoundException(String.format("Grouped Matches %s not found for court %s and caseNo %s", groupId, courtCode, caseNo));
                    }
                    return e;
                });
    }
}
