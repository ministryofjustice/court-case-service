package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderMatchRepository;
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

    public GroupedOffenderMatchesEntity createGroupedMatches(String courtCode, String caseNo, GroupedOffenderMatchesRequest offenderMatches) {
        var courtCase = courtCaseService.getCaseByCaseNumber(courtCode, caseNo);
        GroupedOffenderMatchesEntity matchesEntity = mapper.entityOf(offenderMatches, courtCase);
        offenderMatchRepository.save(matchesEntity);
        return matchesEntity;
    }

    public Mono<GroupedOffenderMatchesEntity> getGroupedMatches(String courtCode, String caseNo, Long groupId) {
        return Mono.justOrEmpty(offenderMatchRepository.findById(groupId));
    }
}
