package uk.gov.justice.probation.courtcaseservice.service.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.MatchIdentifiers;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.service.model.MatchType;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class OffenderMatchMapperTest {

    public static final long CASE_ID = 123L;
    public static final String CASE_NO = "1234";
    public static final String COURT_CODE = "COURT_CODE";

    @Test
    public void givenMultipleMatches_thenMapAllFields() {
        OffenderMatchMapper offenderMatchMapper = new OffenderMatchMapper();
        CourtCaseEntity courtCaseEntity = CourtCaseEntity.builder()
                .id(CASE_ID)
                .caseNo(CASE_NO)
                .courtCode(COURT_CODE)
                .build();
        GroupedOffenderMatchesRequest groupedOffenderMatchesRequest = GroupedOffenderMatchesRequest.builder()
                .matches(Arrays.asList(OffenderMatchRequest.builder()
                            .matchType(MatchType.NAME)
                            .confirmed(false)
                            .matchIdentifiers(new MatchIdentifiers("CRN1", "PNC1", "CRO1"))
                            .build(),
                        OffenderMatchRequest.builder()
                                .matchType(MatchType.NAME)
                                .confirmed(false)
                                .matchIdentifiers(new MatchIdentifiers("CRN2", "PNC2", "CRO2"))
                                .build()))
                .build();
        GroupedOffenderMatchesEntity matchesEntity = offenderMatchMapper.entityOf(groupedOffenderMatchesRequest, courtCaseEntity);

        assertThat(matchesEntity.getOffenderMatches()).hasSize(2);
        assertThat(matchesEntity.getId()).isNull();
        assertThat(matchesEntity.getCourtCaseEntity()).isEqualTo(courtCaseEntity);
        assertThat(matchesEntity.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(matchesEntity.getCaseNo()).isEqualTo(CASE_NO);

        OffenderMatchEntity first = matchesEntity.getOffenderMatches().get(0);
        assertThat(first.getCourtCaseEntity()).isEqualTo(courtCaseEntity);
        assertThat(first.getCaseNo()).isEqualTo(CASE_NO);
        assertThat(first.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(first.getConfirmed()).isEqualTo(false);
        assertThat(first.getCrn()).isEqualTo("CRN1");
        assertThat(first.getPnc()).isEqualTo("PNC1");
        assertThat(first.getCro()).isEqualTo("CRO1");
        assertThat(first.getMatchType()).isEqualTo(MatchType.NAME.toString());

        OffenderMatchEntity second = matchesEntity.getOffenderMatches().get(1);
        assertThat(second.getCaseNo()).isEqualTo(CASE_NO);
        assertThat(second.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(second.getConfirmed()).isEqualTo(false);
        assertThat(second.getCrn()).isEqualTo("CRN2");
        assertThat(second.getPnc()).isEqualTo("PNC2");
        assertThat(second.getCro()).isEqualTo("CRO2");
        assertThat(second.getCourtCaseEntity()).isEqualTo(courtCaseEntity);
        assertThat(second.getMatchType()).isEqualTo(MatchType.NAME.toString());
    }

    @Test
    public void givenZeroMatches_thenMapAllFields() {
        OffenderMatchMapper offenderMatchMapper = new OffenderMatchMapper();
        CourtCaseEntity courtCaseEntity = CourtCaseEntity.builder()
                .id(CASE_ID)
                .build();
        GroupedOffenderMatchesRequest groupedOffenderMatchesRequest = GroupedOffenderMatchesRequest.builder()
                .matches(Collections.emptyList())
                .build();
        GroupedOffenderMatchesEntity matchesEntity = offenderMatchMapper.entityOf(groupedOffenderMatchesRequest, courtCaseEntity);

        assertThat(matchesEntity.getOffenderMatches()).isEmpty();
        assertThat(matchesEntity.getId()).isNull();
    }
}