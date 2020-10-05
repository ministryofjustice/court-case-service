package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRegistrationResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRegistrationsResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Registration;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationMapperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String BASE_MOCK_PATH = "src/test/resources/mocks/__files/offender-registrations/";
    private static CommunityApiRegistrationsResponse REGISTRATIONS;

    @BeforeAll
    static void beforeAll() throws IOException{
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        REGISTRATIONS = OBJECT_MAPPER.readValue(new File(BASE_MOCK_PATH + "GET_registrations_X320741.json"), CommunityApiRegistrationsResponse.class);

    }

    @DisplayName("Mapping of all fields")
    @Test
    void whenMapRegistration_thenReturn() {

        List<Registration> registrations = RegistrationMapper.registrationsFrom(REGISTRATIONS);

        assertThat(registrations).hasSize(4);

        Registration registration = registrations.stream()
            .filter(reg -> reg.getType().equalsIgnoreCase("Suicide/Self Harm"))
            .findFirst()
            .orElse(null);

        assertThat(registration.getNotes()).hasSize(2);
        assertThat(registration.getNotes().get(0)).isEqualTo("Note 1");
        assertThat(registration.getEndDate()).isNull();
        assertThat(registration.getStartDate()).isEqualTo(LocalDate.of(2020, Month.SEPTEMBER, 30));
        assertThat(registration.getNextReviewDate()).isEqualTo(LocalDate.of(2021, Month.MARCH, 30));
        assertThat(registration.isActive()).isTrue();

        assertThat(registrations.stream()
            .filter(reg -> !reg.isActive())
            .findFirst()
            .get().getEndDate()).isEqualTo(LocalDate.of(2019, Month.OCTOBER, 14));
    }

    @DisplayName("Mapping of registration with an unset notes field")
    @Test
    void givenNullNotes_whenMap_thenReturnEmptyListOfNotes() {
        Registration registration = RegistrationMapper.buildRegistration(CommunityApiRegistrationResponse.builder().build());

        assertThat(registration.getNotes()).isEmpty();
    }

    @DisplayName("Mapping of registration with a single note")
    @Test
    void givenSingleNote_whenMap_thenReturnList() {
        Registration registration = RegistrationMapper.buildRegistration(CommunityApiRegistrationResponse.builder()
                                                                                                .notes("NOTE1")
                                                                                                .build());

        assertThat(registration.getNotes()).hasSize(1);
        assertThat(registration.getNotes().get(0)).isEqualTo("NOTE1");
    }
}
