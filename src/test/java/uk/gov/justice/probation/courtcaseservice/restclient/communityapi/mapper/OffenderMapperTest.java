package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderMapperTest {

    private static final String CRN = "CRN";
    private OffenderMapper mapper;
    private CommunityApiOffenderResponse offenderResponse;

    @Before
    public void setUp() throws IOException {
        mapper = new OffenderMapper();
        var objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        offenderResponse = objectMapper.readValue(new File("src/test/resources/mocks/__files/GET_offender_all_X320741.json"), CommunityApiOffenderResponse.class);
    }

    @Test
    public void shouldMapOffenderDetailsToOffender() {

        var offender = mapper.offenderFrom(offenderResponse);

        assertThat(offender.getCrn())
                .isNotNull()
                .isEqualTo("X320741");

        var actualManager = offender.getOffenderManager();
        assertThat(actualManager.getForenames())
                .isNotNull()
                .isEqualTo("Temperance");
        assertThat(actualManager.getSurname())
                .isNotNull()
                .isEqualTo("Brennan");
        assertThat(actualManager.getAllocatedDate())
                .isNotNull()
                .isEqualTo(LocalDate.of(2019, 9, 30));
    }

}