package uk.gov.justice.probation.courtcaseservice.service.mapper;

import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;

@Component
public class OffenderMatchMapper {
    public GroupedOffenderMatchesEntity groupedMatchesOf(GroupedOffenderMatchesRequest offenderMatches, CourtCaseEntity courtCase) {
        var group = GroupedOffenderMatchesEntity.builder()
                .courtCase(courtCase)
                .build();

        group.setOffenderMatches(offenderMatches.getMatches().stream()
                .map(
                        offenderMatchRequest ->
                                        OffenderMatchEntity.builder()
                                                .group(group)
                                                .confirmed(offenderMatchRequest.getConfirmed())
                                                .matchType(offenderMatchRequest.getMatchType())
                                                .crn(offenderMatchRequest.getMatchIdentifiers().getCrn())
                                                .pnc(offenderMatchRequest.getMatchIdentifiers().getPnc())
                                                .cro(offenderMatchRequest.getMatchIdentifiers().getCro())
                                                .build()
                ).collect(Collectors.toList()));
        return group;
    }
}
