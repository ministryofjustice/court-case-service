package uk.gov.justice.probation.courtcaseservice.service.mapper;

import org.jetbrains.annotations.Nullable;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchAlias;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderAliasEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OffenderMatchMapper {

    public static GroupedOffenderMatchesEntity newGroupedMatchesOf(GroupedOffenderMatchesRequest offenderMatches) {
        var group = GroupedOffenderMatchesEntity.builder()
            .build();
        group.setOffenderMatches(buildOffenderMatchEntities(offenderMatches.getMatches(), group));
        return group;
    }

    public static GroupedOffenderMatchesEntity newGroupedMatchesOf(String defendantId, GroupedOffenderMatchesRequest offenderMatches, CourtCaseEntity courtCase) {
        var group = GroupedOffenderMatchesEntity.builder()
                .defendantId(defendantId)
                .caseId(courtCase.getCaseId())
                .build();
        group.setOffenderMatches(buildOffenderMatchEntities(offenderMatches.getMatches(), group));
        return group;
    }

    public static GroupedOffenderMatchesEntity newGroupedMatchesOf(String defendantId, GroupedOffenderMatchesRequest offenderMatches, String caseId) {
        var group = GroupedOffenderMatchesEntity.builder()
                .defendantId(defendantId)
                .caseId(caseId)
                .build();
        group.setOffenderMatches(buildOffenderMatchEntities(offenderMatches.getMatches(), group));
        return group;
    }

    private static List<OffenderMatchEntity> buildOffenderMatchEntities(List<OffenderMatchRequest> offenderMatchRequests, GroupedOffenderMatchesEntity group) {
        return offenderMatchRequests.stream()
            .map(
                offenderMatchRequest ->
                    OffenderMatchEntity.builder()
                        .group(group)
                        .confirmed(offenderMatchRequest.getConfirmed())
                        .rejected(offenderMatchRequest.getRejected())
                        .matchType(offenderMatchRequest.getMatchType())
                        .crn(offenderMatchRequest.getMatchIdentifiers().getCrn())
                        .pnc(offenderMatchRequest.getMatchIdentifiers().getPnc())
                        .cro(offenderMatchRequest.getMatchIdentifiers().getCro())
                        .aliases(mapAliases(offenderMatchRequest.getMatchIdentifiers().getAliases()))
                        .build()
            )
            .collect(Collectors.toList());
    }

    @Nullable
    private static List<OffenderAliasEntity> mapAliases(List<OffenderMatchAlias> offenderMatchAliases) {
        return Optional.ofNullable(offenderMatchAliases).map(aliases ->
            aliases.stream().map(OffenderMatchAlias::asEntity).collect(Collectors.toList())
        ).orElse(null);
    }

    public static GroupedOffenderMatchesEntity update(String caseId, String defendantId, GroupedOffenderMatchesEntity group, GroupedOffenderMatchesRequest request) {
        group.setDefendantId(defendantId);
        group.setCaseId(caseId);
        updateGroupMatches(request.getMatches(), group);
        return group;
    }

    public static GroupedOffenderMatchesEntity update(GroupedOffenderMatchesEntity group, GroupedOffenderMatchesRequest request) {
        updateGroupMatches(request.getMatches(), group);
        return group;
    }

    private static void updateGroupMatches(List<OffenderMatchRequest> matches, GroupedOffenderMatchesEntity group) {
        group.clearOffenderMatches();

        Optional.ofNullable(matches).orElse(Collections.emptyList())
            .stream()
            .map(
                offenderMatchRequest ->
                    OffenderMatchEntity.builder()
                        .group(group)
                        .confirmed(offenderMatchRequest.getConfirmed())
                        .rejected(offenderMatchRequest.getRejected())
                        .matchType(offenderMatchRequest.getMatchType())
                        .crn(offenderMatchRequest.getMatchIdentifiers().getCrn())
                        .pnc(offenderMatchRequest.getMatchIdentifiers().getPnc())
                        .cro(offenderMatchRequest.getMatchIdentifiers().getCro())
                        .aliases(mapAliases(offenderMatchRequest.getMatchIdentifiers().getAliases()))
                        .build()
            )
            .forEach(newMatch -> group.getOffenderMatches().add(newMatch));
    }
}
