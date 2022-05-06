package uk.gov.justice.probation.courtcaseservice.service.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.MatchIdentifiers;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchAlias;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.service.model.MatchType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;

class OffenderMatchMapperTest {

    public static final String CASE_NO = "1234";

    private static OffenderMatchAlias offenderMatchAlias = OffenderMatchAlias.builder()
        .dateOfBirth(LocalDate.now())
        .firstName("firstNameOne")
        .middleNames(List.of("middleOne", "middleTwo"))
        .surname("surnameOne")
        .gender("Not Specified")
        .build();

    private static final OffenderMatchRequest matchRequest1 = OffenderMatchRequest.builder()
            .matchType(MatchType.NAME)
            .confirmed(true)
            .rejected(false)
            .matchIdentifiers(new MatchIdentifiers("CRN1", "PNC1", "CRO1", List.of(offenderMatchAlias)))
            .build();

    private static final OffenderMatchRequest matchRequest2 = OffenderMatchRequest.builder()
        .matchType(MatchType.NAME)
        .confirmed(false)
        .rejected(true)
        .matchIdentifiers(new MatchIdentifiers("CRN2", "PNC2", "CRO2", List.of(offenderMatchAlias)))
        .build();

    @Test
    void givenMultipleMatches_whenNewEntity_thenMapAllFields() {
        var courtCaseEntity = EntityHelper.aHearingEntity(CRN, CASE_NO);
        var groupedOffenderMatchesRequest = GroupedOffenderMatchesRequest.builder()
                .matches(asList(matchRequest1, matchRequest2))
                .build();
        var matchesEntity = OffenderMatchMapper.newGroupedMatchesOf(groupedOffenderMatchesRequest);

        assertThat(matchesEntity.getId()).isNull();

        assertThat(matchesEntity.getOffenderMatches()).hasSize(2);
        checkMatches(matchesEntity.getOffenderMatches().get(0), matchesEntity.getOffenderMatches().get(1), matchesEntity);
    }

    @Test
    void givenMultipleMatches_whenNewEntityForExtended_thenMapAllFields() {
        var courtCaseEntity = EntityHelper.aHearingEntity(CRN, CASE_NO).getCourtCase();
        var groupedOffenderMatchesRequest = GroupedOffenderMatchesRequest.builder()
            .matches(asList(matchRequest1, matchRequest2))
            .build();
        var matchesEntity = OffenderMatchMapper.newGroupedMatchesOf(DEFENDANT_ID, groupedOffenderMatchesRequest, courtCaseEntity);

        assertThat(matchesEntity.getId()).isNull();
        assertThat(matchesEntity.getDefendantId()).isEqualTo(DEFENDANT_ID);
        assertThat(matchesEntity.getCaseId()).isEqualTo(CASE_ID);

        assertThat(matchesEntity.getOffenderMatches()).hasSize(2);
        checkMatches(matchesEntity.getOffenderMatches().get(0), matchesEntity.getOffenderMatches().get(1), matchesEntity);
    }

    @Test
    void givenZeroMatches_whenNewEntity_thenMapAllFields() {
        var courtCaseEntity = EntityHelper.aHearingEntity(CRN, CASE_NO);
        var groupedOffenderMatchesRequest = GroupedOffenderMatchesRequest.builder()
                .matches(Collections.emptyList())
                .build();
        var matchesEntity = OffenderMatchMapper.newGroupedMatchesOf(groupedOffenderMatchesRequest);

        assertThat(matchesEntity.getOffenderMatches()).isEmpty();
        assertThat(matchesEntity.getId()).isNull();
    }

    @Test
    void whenUpdate_thenRefreshMatches() {
        var offenderMatchEntity = OffenderMatchEntity.builder()
            .matchType(MatchType.NAME_DOB)
            .confirmed(true)
            .rejected(false)
            .crn("CRN2")
            .cro("CRO2")
            .pnc("PNC2")
            .build();
        var existingEntity = GroupedOffenderMatchesEntity.builder()
            .id(99L)
            .offenderMatches(new ArrayList<>() {{ add(offenderMatchEntity); }})
            .build();

        var groupedOffenderMatchesRequest = GroupedOffenderMatchesRequest.builder()
            .matches(List.of(matchRequest1))
            .build();
        var matchesEntity = OffenderMatchMapper.update(existingEntity, groupedOffenderMatchesRequest);

        assertThat(matchesEntity.getOffenderMatches()).hasSize(1);
        assertThat(matchesEntity).isSameAs(existingEntity);

        var first = matchesEntity.getOffenderMatches().get(0);
        assertThat(first.getGroup()).isEqualTo(matchesEntity);
        assertThat(first.getConfirmed()).isEqualTo(true);
        assertThat(first.getRejected()).isEqualTo(false);
        assertThat(first.getCrn()).isEqualTo("CRN1");
        assertThat(first.getPnc()).isEqualTo("PNC1");
        assertThat(first.getCro()).isEqualTo("CRO1");
        assertThat(first.getMatchType()).isEqualTo(MatchType.NAME);
    }

    @Test
    void givenExtendedRequest_whenUpdate_thenRefreshMatches() {
        var offenderMatchEntity = OffenderMatchEntity.builder()
            .matchType(MatchType.NAME_DOB)
            .confirmed(true)
            .rejected(false)
            .crn("CRN2")
            .cro("CRO2")
            .pnc("PNC2")
            .build();
        var existingEntity = GroupedOffenderMatchesEntity.builder()
            .id(99L)
            .offenderMatches(new ArrayList<>() {{ add(offenderMatchEntity); }})
            .build();

        var groupedOffenderMatchesRequest = GroupedOffenderMatchesRequest.builder()
            .matches(List.of(matchRequest1))
            .build();
        var matchesEntity = OffenderMatchMapper.update(CASE_ID, DEFENDANT_ID, existingEntity, groupedOffenderMatchesRequest);

        assertThat(matchesEntity.getOffenderMatches()).hasSize(1);
        assertThat(matchesEntity).isSameAs(existingEntity);
        assertThat(matchesEntity.getDefendantId()).isEqualTo(DEFENDANT_ID);
        assertThat(matchesEntity.getCaseId()).isEqualTo(CASE_ID);

        var first = matchesEntity.getOffenderMatches().get(0);
        assertThat(first.getGroup()).isEqualTo(matchesEntity);
        assertThat(first.getConfirmed()).isEqualTo(true);
        assertThat(first.getRejected()).isEqualTo(false);
        assertThat(first.getCrn()).isEqualTo("CRN1");
        assertThat(first.getPnc()).isEqualTo("PNC1");
        assertThat(first.getCro()).isEqualTo("CRO1");
        assertThat(first.getMatchType()).isEqualTo(MatchType.NAME);
    }

    void checkMatches(OffenderMatchEntity match1, OffenderMatchEntity match2, GroupedOffenderMatchesEntity group) {
        assertThat(match1.getGroup()).isSameAs(group);
        assertThat(match1.getConfirmed()).isEqualTo(true);
        assertThat(match1.getRejected()).isEqualTo(false);
        assertThat(match1.getCrn()).isEqualTo("CRN1");
        assertThat(match1.getPnc()).isEqualTo("PNC1");
        assertThat(match1.getCro()).isEqualTo("CRO1");
        assertThat(match1.getMatchType()).isEqualTo(MatchType.NAME);

        assertThat(match2.getGroup()).isSameAs(group);
        assertThat(match2.getConfirmed()).isEqualTo(false);
        assertThat(match2.getRejected()).isEqualTo(true);
        assertThat(match2.getCrn()).isEqualTo("CRN2");
        assertThat(match2.getPnc()).isEqualTo("PNC2");
        assertThat(match2.getCro()).isEqualTo("CRO2");
        assertThat(match2.getMatchType()).isEqualTo(MatchType.NAME);
    }
}
