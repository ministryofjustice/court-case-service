package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiSentence;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class OffenderMapperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String BASE_MOCK_PATH = "src/test/resources/mocks/__files/";

    public static final Requirement EXPECTED_RQMNT_1 = Requirement.builder()
        .requirementId(2500083652L)
        .length(60L)
        .lengthUnit("Hours")
        .startDate(LocalDate.of(2017,6,1))
        .terminationDate(LocalDate.of(2017,12,1))
        .expectedStartDate(LocalDate.of(2017,6,1))
        .expectedEndDate(LocalDate.of(2017,12,1))
        .active(false)
        .requirementTypeSubCategory(new KeyValue("W01", "Regular"))
        .requirementTypeMainCategory(new KeyValue("W", "Unpaid Work"))
        .terminationReason(new KeyValue("74", "Hours Completed Outside 12 months (UPW only)"))
        .build();

    public static final Requirement EXPECTED_RQMNT_2 = Requirement.builder()
        .requirementId(2500007925L)
        .startDate(LocalDate.of(2015,7,1))
        .commencementDate(LocalDate.of(2015,6,29))
        .active(true)
        .adRequirementTypeMainCategory(new KeyValue("7", "Court - Accredited Programme"))
        .adRequirementTypeSubCategory(new KeyValue("P12", "ASRO"))
        .build();

    private OffenderMapper mapper;

    @BeforeAll
    public static void setUpBeforeClass() {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setUp() {
        mapper = new OffenderMapper();
    }

    @DisplayName("Maps community API offender to ProbationRecord with manager")
    @Test
    void shouldMapOffenderDetailsToOffender() throws IOException {

        CommunityApiOffenderResponse offenderResponse
            = OBJECT_MAPPER.readValue(new File(BASE_MOCK_PATH + "GET_offender_all_X320741.json"), CommunityApiOffenderResponse.class);

        var offender = mapper.probationRecordFrom(offenderResponse);

        assertThat(offender.getCrn())
                .isNotNull()
                .isEqualTo("X320741");

        assertThat(offender.getConvictions()).isNull();
        assertThat(offender.getOffenderManagers().size()).isEqualTo(1);
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

    @DisplayName("Maps convictions response to court case service conviction. Includes sentence and unpaid work.")
    @Test
    void shouldMapConvictionDetailsToConviction() throws IOException {

        CommunityApiConvictionsResponse convictionsResponse
            = OBJECT_MAPPER
            .readValue(new File(BASE_MOCK_PATH + "offender-convictions/GET_offender_convictions_X320741.json"), CommunityApiConvictionsResponse.class);

        List<Conviction> convictions = mapper.convictionsFrom(convictionsResponse);

        assertThat(convictions).hasSize(3);
        Conviction conviction1 = convictions.get(0);
        assertThat(conviction1.getConvictionId()).isEqualTo("2500297061");
        assertThat(conviction1.getActive()).isEqualTo(false);
        assertThat(conviction1.getInBreach()).isTrue();
        assertThat(conviction1.getConvictionDate()).isEqualTo(LocalDate.of(2019,9,16));

        assertThat(conviction1.getOffences()).hasSize(1);
        assertThat(conviction1.getOffences().get(0).getDescription()).isEqualTo("Assault on Police Officer - 10400");

        assertThat(conviction1.getSentence().getSentenceId()).isEqualTo("123456");
        assertThat(conviction1.getSentence().getDescription()).isEqualTo("Absolute/Conditional Discharge");
        assertThat(conviction1.getSentence().getLength()).isEqualTo(0);
        assertThat(conviction1.getSentence().getLengthUnits()).isEqualTo("Months");
        assertThat(conviction1.getSentence().getLengthInDays()).isEqualTo(0);
        assertThat(conviction1.getSentence().getTerminationDate()).isEqualTo(LocalDate.of(2020,1,1));
        assertThat(conviction1.getSentence().getTerminationReason()).isEqualTo("Auto Terminated");

        assertThat(conviction1.getSentence().getUnpaidWork().getMinutesOffered()).isEqualTo(480);
        assertThat(conviction1.getSentence().getUnpaidWork().getMinutesCompleted()).isEqualTo(60);
        assertThat(conviction1.getSentence().getUnpaidWork().getAppointmentsToDate()).isEqualTo(12);
        assertThat(conviction1.getSentence().getUnpaidWork().getAttended()).isEqualTo(2);
        assertThat(conviction1.getSentence().getUnpaidWork().getAcceptableAbsences()).isEqualTo(2);
        assertThat(conviction1.getSentence().getUnpaidWork().getUnacceptableAbsences()).isEqualTo(1);

        // conviction date + sentence.lengthInDays
        assertThat(conviction1.getEndDate()).isEqualTo(LocalDate.of(2019,9,16).plus(Duration.ofDays(0)));

        Conviction conviction2 = convictions.get(1);
        assertThat(conviction2.getConvictionId()).isEqualTo("2500295345");
        assertThat(conviction2.getInBreach()).isFalse();
        assertThat(conviction2.getSentence().getSentenceId()).isEqualTo("123457");
        assertThat(conviction2.getSentence().getDescription()).isEqualTo("CJA - Indeterminate Public Prot.");
        assertThat(conviction2.getSentence().getTerminationDate()).isEqualTo(LocalDate.of(2019,1,1));
        assertThat(conviction2.getSentence().getTerminationReason()).isEqualTo("ICMS Miscellaneous Event");
        assertThat(conviction2.getSentence().getLengthInDays()).isEqualTo(1826);
        assertThat(conviction2.getSentence().getUnpaidWork()).isNull();
        // conviction date + sentence.lengthInDays
        assertThat(conviction2.getEndDate()).isEqualTo(LocalDate.of(2019,9,3).plusDays(1826));

        Conviction conviction3 = convictions.get(2);
        assertThat(conviction3.getConvictionId()).isEqualTo("2500295343");
        assertThat(conviction3.getEndDate()).isNull();
        assertThat(conviction3.getSentence()).isNull();
    }

    @DisplayName("Maps convictions response to court case service conviction. Empty Offence list and all nullable fields null.")
    @Test
    void shouldMapConvictionDetailsToConvictionNull() {

        final CommunityApiConvictionResponse convictionResponse = CommunityApiConvictionResponse
                .builder()
                .convictionId("123")
                .offences(Collections.emptyList())
                .build();
        final CommunityApiConvictionsResponse convictionsResponse = new CommunityApiConvictionsResponse(singletonList(convictionResponse));

        final List<Conviction> convictions = mapper.convictionsFrom(convictionsResponse);

        final Conviction expectedConviction = Conviction.builder()
            .active(null)
            .convictionDate(null)
            .sentence(null)
            .endDate(null)
            .convictionId("123")
            .offences(Collections.emptyList())
            .build();

        assertThat(convictions).hasSize(1);
        assertThat(convictions.get(0)).isEqualToComparingFieldByField(expectedConviction);
    }

    @DisplayName("Maps convictions response to court case service conviction.")
    @Test
    void shouldMapConvictionDetailsToConvictionSentenceSetNullUnpaidWord() {

        final CommunityApiConvictionResponse convictionResponse = CommunityApiConvictionResponse
            .builder()
            .convictionId("123")
            .offences(Collections.emptyList())
            .build();
        final CommunityApiConvictionsResponse convictionsResponse = new CommunityApiConvictionsResponse(singletonList(convictionResponse));

        final List<Conviction> convictions = mapper.convictionsFrom(convictionsResponse);

        final Conviction expectedConviction = Conviction.builder()
            .active(null)
            .convictionDate(null)
            .sentence(null)
            .endDate(null)
            .convictionId("123")
            .offences(Collections.emptyList())
            .build();

        assertThat(convictions).hasSize(1);
        assertThat(convictions.get(0)).isEqualToComparingFieldByField(expectedConviction);
    }

    @DisplayName("No end date if convictionDate and sentence are null")
    @Test
    void endDateCalculatorNulls() {
        assertThat(mapper.endDateCalculator.apply(null, null)).isNull();
    }

    @DisplayName("No end date if sentence is null")
    @Test
    void endDateCalculatorNullSentence() {
        assertThat(mapper.endDateCalculator.apply(LocalDate.of(2019, 10, 1), null)).isNull();
    }

    @DisplayName("No end date if length of sentence is null")
    @Test
    void endDateCalculatorNullSentenceLength() {
        final CommunityApiSentence sentence = CommunityApiSentence.builder().build();
        assertThat(mapper.endDateCalculator.apply(LocalDate.of(2019, 10, 1), sentence)).isNull();
    }

    @DisplayName("No end date if convictionDate is null")
    @Test
    void endDateCalculatorNullDate() {
        final CommunityApiSentence sentence = CommunityApiSentence.builder().lengthInDays(1).build();
        assertThat(mapper.endDateCalculator.apply(null, sentence)).isNull();
    }

    @DisplayName("No end date if convictionDate is null")
    @Test
    void endDateCalculatorNormal() {
        final CommunityApiSentence sentence = CommunityApiSentence.builder().lengthInDays(1).build();
        assertThat(mapper.endDateCalculator.apply(LocalDate.of(2019, 10, 1), sentence)).isEqualTo(LocalDate.of(2019, 10, 2));
    }

    @DisplayName("Tests mapping of Community API requirements to Court Case Service equivalent")
    @Test
    void shouldMapRequirementDetailsToRequirement() throws IOException {

        CommunityApiRequirementsResponse requirementsResponse
            = OBJECT_MAPPER.readValue(new File(BASE_MOCK_PATH + "offender-requirements/GET_requirements_X320741.json"),
            CommunityApiRequirementsResponse.class);
        List<Requirement> requirements = mapper.requirementsFrom(requirementsResponse);

        assertThat(requirements).hasSize(2);

        final Requirement rqmt1 = requirements.stream()
            .filter(requirement -> requirement.getRequirementId().equals(2500083652L))
            .findFirst().orElse(null);
        final Requirement rqmt2 = requirements.stream()
            .filter(requirement -> requirement.getRequirementId().equals(2500007925L))
            .findFirst().orElse(null);
        assertThat(EXPECTED_RQMNT_1).isEqualToComparingFieldByField(rqmt1);
        assertThat(EXPECTED_RQMNT_2).isEqualToComparingFieldByField(rqmt2);
    }

    @DisplayName("Tests empty requirements list.")
    @Test
    void shouldMapEmpty() throws IOException {
        CommunityApiRequirementsResponse emptyResponse = OBJECT_MAPPER
            .readValue(new File(BASE_MOCK_PATH + "offender-requirements/GET_requirements_X320741_empty.json"), CommunityApiRequirementsResponse.class);

        assertThat(emptyResponse.getRequirements()).isEmpty();
    }

}
