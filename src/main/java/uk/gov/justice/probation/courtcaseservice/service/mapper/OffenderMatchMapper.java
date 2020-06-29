package uk.gov.justice.probation.courtcaseservice.service.mapper;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;

import java.util.stream.Collectors;

@Component
public class OffenderMatchMapper {
    public GroupedOffenderMatchesEntity entityOf(GroupedOffenderMatchesRequest offenderMatches, CourtCaseEntity courtCase) {
        return GroupedOffenderMatchesEntity.builder()
                .offenderMatches(offenderMatches.getMatches()
                        .stream()
                        .map(offenderMatchRequest ->
                            OffenderMatchEntity.builder()
                                    .courtCaseEntity(courtCase)
                                    .caseNo(courtCase.getCaseNo())
                                    .courtCode(courtCase.getCourtCode())
                                    .confirmed(offenderMatchRequest.getConfirmed())
                                    .matchType(offenderMatchRequest.getMatchType().toString())
                                    .crn(offenderMatchRequest.getMatchIdentifiers().getCrn())
                                    .pnc(offenderMatchRequest.getMatchIdentifiers().getPnc())
                                    .cro(offenderMatchRequest.getMatchIdentifiers().getCro())
                                    .build()
                        ).collect(Collectors.toList())
                )
                .courtCaseEntity(courtCase)
                .courtCode(courtCase.getCourtCode())
                .caseNo(courtCase.getCaseNo())
                .build();
    }
}
