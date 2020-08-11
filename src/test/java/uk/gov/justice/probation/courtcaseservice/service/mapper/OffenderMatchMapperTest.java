package uk.gov.justice.probation.courtcaseservice.service.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.MatchIdentifiers;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
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
        var offenderMatchMapper = new OffenderMatchMapper();
        var courtCaseEntity = CourtCaseEntity.builder()
                .id(CASE_ID)
                .caseNo(CASE_NO)
                .courtCode(COURT_CODE)
                .build();
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
        var matchesEntity = offenderMatchMapper.groupedMatchesOf(groupedOffenderMatchesRequest, courtCaseEntity);

        assertThat(matchesEntity.getOffenderMatches()).hasSize(2);
        assertThat(matchesEntity.getId()).isNull();
        assertThat(matchesEntity.getCourtCase()).isEqualTo(courtCaseEntity);

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
        var offenderMatchMapper = new OffenderMatchMapper();
        var courtCaseEntity = CourtCaseEntity.builder()
                .id(CASE_ID)
                .build();
        var groupedOffenderMatchesRequest = GroupedOffenderMatchesRequest.builder()
                .matches(Collections.emptyList())
                .build();
        var matchesEntity = offenderMatchMapper.groupedMatchesOf(groupedOffenderMatchesRequest, courtCaseEntity);

        assertThat(matchesEntity.getOffenderMatches()).isEmpty();
        assertThat(matchesEntity.getId()).isNull();
    }
}