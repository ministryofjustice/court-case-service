package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.interventions.BreachMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiResponse;

import static org.assertj.core.api.Assertions.assertThat;

class BreachMapperTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String BASE_MOCK_PATH = "src/test/resources/mocks/__files/offender-interventions/";

    @BeforeAll
    public static void setUpBeforeClass() {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    @DisplayName("maps NSI response to breach")
    @Test
    void shouldMapNsiToBreach() throws IOException {
        CommunityApiNsiResponse nsiResponse
            = OBJECT_MAPPER.readValue(new File(BASE_MOCK_PATH + "GET_nsis_breach_X320741.json"), CommunityApiNsiResponse.class);

        var breaches = BreachMapper.breachesFrom(nsiResponse);
        assertThat(breaches.size()).isEqualTo(1);
        assertThat(breaches.get(0).getBreachId()).isEqualTo(11131321);
        assertThat(breaches.get(0).getDescription()).isEqualTo("Community Order");
        assertThat(breaches.get(0).getStatus()).isEqualTo("Breach Initiated");
        assertThat(breaches.get(0).getStarted()).isEqualTo(LocalDate.of(2019, 10, 20));
        assertThat(breaches.get(0).getStatusDate()).isEqualTo(LocalDate.of(2019, 12, 18));
    }
}
