package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCourtAppearanceResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCourtAppearancesResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.CourtAppearance;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

import static org.assertj.core.api.Assertions.assertThat;

public class CourtAppearanceMapperTest {

    private static final String COURT_NAME = "Sheffield Magistrates' Court";
    private static final String COURT_CODE = "B14LO00";

    @DisplayName("Mapping of normal collection of court appearances")
    @Test
    void shouldMapCourtAppearances() {
        LocalDateTime now = LocalDateTime.now();
        CommunityApiCourtAppearanceResponse appearance1 = buildCourtAppearance(200L, now, "S");
        CommunityApiCourtAppearanceResponse appearance2 = buildCourtAppearance(201L, now, "M");
        CommunityApiCourtAppearancesResponse response = CommunityApiCourtAppearancesResponse.builder()
            .courtAppearances(List.of(appearance1, appearance2))
            .build();

        List<CourtAppearance> appearances = CourtAppearanceMapper.appearancesFrom(response);

        assertThat(appearances).hasSize(2);
        assertThat(appearances).extracting("courtCode").containsExactly(COURT_CODE, COURT_CODE);
        assertThat(appearances).extracting("courtName").containsExactly(COURT_NAME, COURT_NAME);
        assertThat(appearances).extracting("date").containsExactly(now, now);
        assertThat(appearances).extracting("type").containsExactly(new KeyValue("S", "CODE"),
                                                                                new KeyValue("M", "CODE"));
    }

    @DisplayName("Mapping of null collection of court appearances")
    @Test
    void givenNullInputList_whenMap_thenReturnEmptyList() {

        List<CourtAppearance> appearances = CourtAppearanceMapper.appearancesFrom(CommunityApiCourtAppearancesResponse.builder().build());

        assertThat(appearances).hasSize(0);
    }

    static CommunityApiCourtAppearanceResponse buildCourtAppearance(Long id, LocalDateTime now, String type) {
        return CommunityApiCourtAppearanceResponse.builder()
            .appearanceDate(now)
            .appearanceType(new KeyValue(type, "CODE"))
            .courtAppearanceId(id)
            .courtCode(COURT_CODE)
            .courtName(COURT_NAME)
            .crn("X320741")
            .build();
    }

}
