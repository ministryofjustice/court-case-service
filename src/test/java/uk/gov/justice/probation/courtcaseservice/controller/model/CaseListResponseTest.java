package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.Test;
import uk.gov.justice.probation.courtcaseservice.controller.mapper.CourtCaseResponseMapper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseListResponseTest {
    @Test
    public void whenGetLastUpdated_thenReturnMostRecentUpdatedDate() {
        LocalDateTime mostRecent = LocalDateTime.of(2020, 1, 31, 0, 0);
        LocalDateTime sessionStartTime = LocalDateTime.of(2020, 1, 31, 9, 0);
        List<CourtCaseResponse> cases = Arrays.asList(buildCourtCaseEntity(mostRecent, sessionStartTime), buildCourtCaseEntity(LocalDateTime.MIN, sessionStartTime));
        CaseListResponse caseListResponse = new CaseListResponse(cases);

        assertThat(caseListResponse.getLastUpdated()).isEqualTo(mostRecent);
    }

    @Test
    public void givenEmptyCaseList_whenGetLastUpdated_thenReturnNull() {
        CaseListResponse caseListResponse = new CaseListResponse(Collections.emptyList());

        assertThat(caseListResponse.getLastUpdated()).isNull();
    }

    private CourtCaseResponse buildCourtCaseEntity(LocalDateTime mostRecent, LocalDateTime sessionStartTime) {
        return new CourtCaseResponseMapper().mapFrom(CourtCaseEntity.builder()
            .lastUpdated(mostRecent)
            .sessionStartTime(sessionStartTime)
            .offences(Collections.emptyList()).build());
    }
}
