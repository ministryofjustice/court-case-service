package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCommunityOrPrisonOffenderManager;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCommunityOrPrisonOffenderManagerResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiTeam;

import static org.assertj.core.api.Assertions.assertThat;

class OffenderManagerMapperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String BASE_MOCK_PATH = "src/test/resources/mocks/__files/offender-managers/";

    @BeforeAll
    public static void setUpBeforeClass() {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    @Test
    void whenListFromCommunityApiIsNull_whenMap_thenReturnEmptyList() {
        var response = CommunityApiCommunityOrPrisonOffenderManagerResponse.builder().build();

        assertThat(OffenderManagerMapper.offenderManagersFrom(response)).isEmpty();
    }

    @Test
    void whenListFromCommunityApiIsOnlyPrisonOffenderManagers_whenMap_thenReturnEmptyList() {
        var response = CommunityApiCommunityOrPrisonOffenderManagerResponse.builder()
            .offenderManagers(List.of(CommunityApiCommunityOrPrisonOffenderManager.builder().isPrisonOffenderManager(true).build()))
            .build();

        assertThat(OffenderManagerMapper.offenderManagersFrom(response)).isEmpty();
    }

    @Test
    void whenMapOffenderManagers_thenReturn() throws IOException {
        var response
            = OBJECT_MAPPER.readValue(new File(BASE_MOCK_PATH + "GET_offender_managers_X320741.json"), CommunityApiCommunityOrPrisonOffenderManagerResponse.class);

        var offenderManagers = OffenderManagerMapper.offenderManagersFrom(response);
        assertThat(offenderManagers).hasSize(1);
        var offenderManager = offenderManagers.get(0);
        assertThat(offenderManager.getStaff().getForenames()).isEqualTo("JIM");
        assertThat(offenderManager.getStaff().getSurname()).isEqualTo("SNOW");
        assertThat(offenderManager.getStaff().getEmail()).isEqualTo("jim.snow@justice.gov.uk");
        assertThat(offenderManager.getStaff().getTelephone()).isEqualTo("01512112121");
        assertThat(offenderManager.getProvider()).isEqualTo("Essex");
        assertThat(offenderManager.getAllocatedDate()).isEqualTo(LocalDate.of(2018, Month.MAY, 4));
        assertThat(offenderManager.getTeam().getDescription()).isEqualTo("Team desc");
        assertThat(offenderManager.getTeam().getDistrict()).isEqualTo("Team district desc");
        assertThat(offenderManager.getTeam().getTelephone()).isEqualTo("02033334444");
        assertThat(offenderManager.getTeam().getLocalDeliveryUnit()).isEqualTo("LDU desc");
    }

    @Test
    void givenNullsForKeyValues_whenMapTeam_thenReturn() {

        var communityApiTeam = CommunityApiTeam.builder()
            .description("Team desc")
            .telephone("020 1111 2222")
            .build();

        var team = OffenderManagerMapper.teamOf(communityApiTeam);

        assertThat(team.getDescription()).isEqualTo("Team desc");
        assertThat(team.getTelephone()).isEqualTo("020 1111 2222");
        assertThat(team.getDistrict()).isNull();
        assertThat(team.getLocalDeliveryUnit()).isNull();
    }
}
