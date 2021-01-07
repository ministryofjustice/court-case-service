package uk.gov.justice.probation.courtcaseservice.service.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.MatchIdentifiers;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.service.model.MatchType;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;

class OffenderMatchMapperTest {

    public static final long CASE_ID = 123L;
    public static final String CASE_NO = "1234";

    @Test
    public void givenMultipleMatches_thenMapAllFields() {
        var courtCaseEntity = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);
        var groupedOffenderMatchesRequest = GroupedOffenderMatchesRequest.builder()
                .matches(Arrays.asList(OffenderMatchRequest.builder()
                            .matchType(MatchType.NAME)
                            .confirmed(true)
                            .rejected(false)
                            .matchIdentifiers(new MatchIdentifiers("CRN1", "PNC1", "CRO1"))
                            .build(),
                        OffenderMatchRequest.builder()
                                .matchType(MatchType.NAME)
                                .confirmed(false)
                                .rejected(true)
                                .matchIdentifiers(new MatchIdentifiers("CRN2", "PNC2", "CRO2"))
                                .build()))
                .build();
        var matchesEntity = OffenderMatchMapper.newGroupedMatchesOf(groupedOffenderMatchesRequest, courtCaseEntity);

        assertThat(matchesEntity.getOffenderMatches()).hasSize(2);
        assertThat(matchesEntity.getId()).isNull();
        assertThat(matchesEntity.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(matchesEntity.getCaseNo()).isEqualTo(CASE_NO);

        var first = matchesEntity.getOffenderMatches().get(0);
        assertThat(first.getGroup()).isEqualTo(matchesEntity);
        assertThat(first.getConfirmed()).isEqualTo(true);
        assertThat(first.getRejected()).isEqualTo(false);
        assertThat(first.getCrn()).isEqualTo("CRN1");
        assertThat(first.getPnc()).isEqualTo("PNC1");
        assertThat(first.getCro()).isEqualTo("CRO1");
        assertThat(first.getMatchType()).isEqualTo(MatchType.NAME);

        var second = matchesEntity.getOffenderMatches().get(1);
        assertThat(second.getGroup()).isEqualTo(matchesEntity);
        assertThat(second.getConfirmed()).isEqualTo(false);
        assertThat(second.getRejected()).isEqualTo(true);
        assertThat(second.getCrn()).isEqualTo("CRN2");
        assertThat(second.getPnc()).isEqualTo("PNC2");
        assertThat(second.getCro()).isEqualTo("CRO2");
        assertThat(second.getMatchType()).isEqualTo(MatchType.NAME);
    }

    @Test
    public void givenZeroMatches_thenMapAllFields() {
        var courtCaseEntity = CourtCaseEntity.builder()
                .id(CASE_ID)
                .build();
        var groupedOffenderMatchesRequest = GroupedOffenderMatchesRequest.builder()
                .matches(Collections.emptyList())
                .build();
        var matchesEntity = OffenderMatchMapper.newGroupedMatchesOf(groupedOffenderMatchesRequest, courtCaseEntity);

        assertThat(matchesEntity.getOffenderMatches()).isEmpty();
        assertThat(matchesEntity.getId()).isNull();
    }

    @Test
    public void whenUpdate_thenRefreshMatches() {
        var courtCaseEntity = CourtCaseEntity.builder()
            .id(CASE_ID)
            .caseNo(CASE_NO)
            .courtCode(COURT_CODE)
            .build();

        var offenderMatchEntity = OffenderMatchEntity.builder()
            .matchType(MatchType.NAME_DOB)
            .confirmed(true)
            .rejected(false)
            .crn("CRN2")
            .cro("CRO2")
            .pnc("PNC2")
            .build();
        List<OffenderMatchEntity> offenderMatchEntities = new ArrayList<>();
        offenderMatchEntities.add(offenderMatchEntity);
        GroupedOffenderMatchesEntity existingEntity = GroupedOffenderMatchesEntity.builder()
            .courtCode(COURT_CODE)
            .caseNo(CASE_NO)
            .id(99L)
            .offenderMatches(offenderMatchEntities)
            .build();

        var groupedOffenderMatchesRequest = GroupedOffenderMatchesRequest.builder()
            .matches(List.of(OffenderMatchRequest.builder()
                    .matchType(MatchType.NAME)
                    .confirmed(true)
                    .rejected(false)
                    .matchIdentifiers(new MatchIdentifiers("CRN3", "PNC3", "CRO3"))
                    .build()))
            .build();
        var matchesEntity = OffenderMatchMapper.update(existingEntity, groupedOffenderMatchesRequest);

        assertThat(matchesEntity.getOffenderMatches()).hasSize(1);
        assertThat(matchesEntity).isSameAs(existingEntity);
        assertThat(matchesEntity.getCourtCode()).isSameAs(COURT_CODE);
        assertThat(matchesEntity.getCaseNo()).isSameAs(CASE_NO);

        var first = matchesEntity.getOffenderMatches().get(0);
        assertThat(first.getGroup()).isEqualTo(matchesEntity);
        assertThat(first.getConfirmed()).isEqualTo(true);
        assertThat(first.getRejected()).isEqualTo(false);
        assertThat(first.getCrn()).isEqualTo("CRN3");
        assertThat(first.getPnc()).isEqualTo("PNC3");
        assertThat(first.getCro()).isEqualTo("CRO3");
        assertThat(first.getMatchType()).isEqualTo(MatchType.NAME);
    }

}
