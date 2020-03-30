package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderMapperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private OffenderMapper mapper;

    @BeforeClass
    public static void setUpBeforeClass() {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Before
    public void setUp() {
        mapper = new OffenderMapper();
    }

    @Test
    public void shouldMapOffenderDetailsToOffender() throws IOException {

        CommunityApiOffenderResponse offenderResponse
            = OBJECT_MAPPER.readValue(new File("src/test/resources/mocks/__files/GET_offender_all_X320741.json"), CommunityApiOffenderResponse.class);

        var offender = mapper.offenderFrom(offenderResponse);

        assertThat(offender.getCrn())
                .isNotNull()
                .isEqualTo("X320741");

        var actualManager = offender.getOffenderManagers().get(0);
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

    @Test
    public void shouldMapConvictionDetailsToConviction() throws IOException {

        CommunityApiConvictionsResponse convictionsResponse
            = OBJECT_MAPPER
            .readValue(new File("src/test/resources/mocks/__files/GET_offender_convictions_X320741.json"), CommunityApiConvictionsResponse.class);

        List<Conviction> convictions = mapper.convictionsFrom(convictionsResponse);

        assertThat(convictions).hasSize(3);
        Conviction conviction1 = convictions.get(0);
        assertThat(conviction1.getConvictionId()).isEqualTo("2500297061");
        assertThat(conviction1.getActive()).isEqualTo(false);
        assertThat(conviction1.getConvictionDate()).isEqualTo(LocalDate.of(2019,9,16));

        assertThat(conviction1.getOffences()).hasSize(1);
        assertThat(conviction1.getOffences().get(0).getDescription()).isEqualTo("Assault on Police Officer - 10400");

        assertThat(conviction1.getSentence().getDescription()).isEqualTo("Absolute/Conditional Discharge");
        assertThat(conviction1.getSentence().getLength()).isEqualTo(0);
        assertThat(conviction1.getSentence().getLengthUnits()).isEqualTo("Months");
        assertThat(conviction1.getSentence().getLengthInDays()).isEqualTo(0);

        // conviction date + sentence.lengthInDays
        assertThat(conviction1.getEndDate()).isEqualTo(LocalDate.of(2019,9,16).plus(Duration.ofDays(0)));

        Conviction conviction2 = convictions.get(1);
        assertThat(conviction2.getConvictionId()).isEqualTo("2500295345");
        assertThat(conviction2.getSentence().getDescription()).isEqualTo("CJA - Indeterminate Public Prot.");

        Conviction conviction3 = convictions.get(2);
        assertThat(conviction3.getConvictionId()).isEqualTo("2500295343");
        assertThat(conviction3.getSentence().getDescription()).isEqualTo("CJA - Community Order");

        assertThat(conviction3.getSentence().getLength()).isEqualTo(12);
        assertThat(conviction3.getSentence().getLengthUnits()).isEqualTo("Months");
        assertThat(conviction3.getSentence().getLengthInDays()).isEqualTo(364);

        assertThat(conviction3.getEndDate()).isEqualTo(LocalDate.of(2017,6,1).plus(364, ChronoUnit.DAYS));
    }

    @Test
    public void shouldMapRequirementDetailsToRequirement() throws IOException {

        CommunityApiRequirementsResponse requirementsResponse
            = OBJECT_MAPPER.readValue(new File("src/test/resources/mocks/__files/GET_offender_requirements_X320741.json"),
            CommunityApiRequirementsResponse.class);
        List<Requirement> requirements = mapper.requirementsFrom(requirementsResponse);

        assertThat(requirements).hasSize(3);
        Requirement requirement1 = requirements.get(0);
        assertThat(requirement1.getRqmntTypeMainCategoryId()).isEqualTo("11");
        assertThat(requirement1.getRqmntTypeSubCategoryId()).isEqualTo("1256");
        assertThat(requirement1.getAdRqmntTypeMainCategoryId()).isEqualTo(null);
        assertThat(requirement1.getAdRqmntTypeSubCategoryId()).isEqualTo(null);
        assertThat(requirement1.getLength()).isEqualTo(60);
        assertThat(requirement1.getStartDate()).isEqualTo(LocalDate.of(2017,6,01));
        assertThat(requirement1.getTerminationDate()).isEqualTo(LocalDate.of(2017,12,01));
        assertThat(requirement1.getRqmntTerminationReasonId()).isEqualTo("2500052883");

        Requirement requirement2 = requirements.get(1);
        assertThat(requirement2.getRqmntTypeMainCategoryId()).isEqualTo("12345677");
        assertThat(requirement2.getRqmntTypeSubCategoryId()).isEqualTo("1256");
        assertThat(requirement2.getAdRqmntTypeMainCategoryId()).isEqualTo(null);
        assertThat(requirement2.getAdRqmntTypeSubCategoryId()).isEqualTo(null);
        assertThat(requirement2.getLength()).isEqualTo(60);
        assertThat(requirement2.getStartDate()).isEqualTo(LocalDate.of(2019,6,01));
        assertThat(requirement2.getTerminationDate()).isEqualTo(LocalDate.of(2019,12,01));
        assertThat(requirement2.getRqmntTerminationReasonId()).isEqualTo("2500052885");

        Requirement requirement3 = requirements.get(2);
        assertThat(requirement3.getRqmntTypeMainCategoryId()).isEqualTo("1778990");
        assertThat(requirement3.getRqmntTypeSubCategoryId()).isEqualTo("1256789");
        assertThat(requirement3.getAdRqmntTypeMainCategoryId()).isEqualTo(null);
        assertThat(requirement3.getAdRqmntTypeSubCategoryId()).isEqualTo(null);
        assertThat(requirement3.getLength()).isEqualTo(60);
        assertThat(requirement3.getStartDate()).isEqualTo(LocalDate.of(2018,6,01));
        assertThat(requirement3.getTerminationDate()).isEqualTo(LocalDate.of(2018,12,01));
        assertThat(requirement3.getRqmntTerminationReasonId()).isEqualTo("2500052884");
    }

    @Test
    public void shouldMapEmpty() throws IOException {
        CommunityApiRequirementsResponse emptyResponse = OBJECT_MAPPER
            .readValue(new File("src/test/resources/mocks/__files/GET_offender_requirements_X320741_empty.json"), CommunityApiRequirementsResponse.class);

        assertThat(emptyResponse.getRequirements()).isEmpty();
    }
}
