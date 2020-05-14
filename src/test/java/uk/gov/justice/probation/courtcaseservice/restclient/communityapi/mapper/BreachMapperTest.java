package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.interventions.BreachMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiResponse;

class BreachMapperTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static BreachMapper mapper;
    private static final String BASE_MOCK_PATH = "src/test/resources/mocks/__files/offender-interventions";

    @BeforeEach
    void beforeEach() {
        mapper = new BreachMapper();
    }

    @DisplayName("maps NSI response to breach")
    @Test
    void shouldMapNsiToBreach() throws IOException {
        CommunityApiNsiResponse nsiResponse
            = OBJECT_MAPPER.readValue(new File(BASE_MOCK_PATH + "GET_nsis_breach_X320741.json"), CommunityApiNsiResponse.class);

        var breaches = mapper.breachesFrom(nsiResponse);
        assertThat(breaches.size()).isEqualTo(1);
        assertThat(breaches.get(0).getId()).isEqualTo(11131321);
        assertThat(breaches.get(0).getDescription()).isEqualTo("Community Order");
        assertThat(breaches.get(0).getStatus()).isEqualTo("Breach Initiated");
        assertThat(breaches.get(0).getStarted()).isEqualTo(LocalDate.of(2019, 10, 20));
    }
}
