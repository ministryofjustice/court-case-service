package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.CurrentOrderHeaderResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetail;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCustodialStatusResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiSentence;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class OffenderMapperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String BASE_MOCK_PATH = "src/test/resources/mocks/__files/";

    private static CommunityApiOffenderResponse offenderResponse;

    private OffenderMapper mapper;

    @BeforeAll
    public static void setUpBeforeClass() throws IOException {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());

        offenderResponse
            = OBJECT_MAPPER.readValue(new File(BASE_MOCK_PATH + "GET_offender_all_X320741.json"), CommunityApiOffenderResponse.class);
    }

    @BeforeEach
    void setUp() {
        mapper = new OffenderMapper();
    }

    @DisplayName("Maps community API offender to ProbationRecord with manager")
    @Test
    void shouldMapOffenderProbationRecordDetailsToOffender() {

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

    @DisplayName("Maps community API offender to OffenderDetail")
    @Test
    void shouldMapOffenderDetailsToOffenderDetail() {
        offenderResponse.setDateOfBirth(null);

        var offenderDetail = mapper.offenderDetailFrom(offenderResponse);

        assertThat(offenderDetail.getOtherIds().getCrn())
            .isNotNull()
            .isEqualTo("X320741");
        assertThat(offenderDetail.getTitle()).isEqualTo("Mr.");
        assertThat(offenderDetail.getOtherIds().getCrn()).isEqualTo("X320741");
        assertThat(offenderDetail.getMiddleNames()).containsExactlyInAnyOrder("Hope", "Felix");
        assertThat(offenderDetail.getProbationStatus()).isSameAs(ProbationStatus.CURRENT);
        assertThat(offenderDetail.getDateOfBirth()).isNull();
        assertThat(offenderDetail.getForename()).isEqualTo("Aadland");
        assertThat(offenderDetail.getSurname()).isEqualTo("Bertrand");
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

    @DisplayName("Maps convictions response to court case service conviction. Null offence list and all nullable fields null.")
    @Test
    void shouldMapConvictionDetailsToConvictionNull() {

        final CommunityApiConvictionResponse convictionResponse = CommunityApiConvictionResponse
                .builder()
                .convictionId("123")
                .offences(null)
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

    @DisplayName("Test custodial status mapping of values")
    @Test
    void shouldMapFullCustodialStatus() {
        LocalDate actualReleaseDate = LocalDate.of(2020, 8, 5);
        LocalDate licenceExpiryDate = LocalDate.of(2020, 8, 6);
        LocalDate pssEndDate = LocalDate.of(2020, 8, 7);
        LocalDate sentenceDate = LocalDate.of(2020, 8, 8);
        CommunityApiCustodialStatusResponse response = CommunityApiCustodialStatusResponse.builder()
                .sentenceId(1234L)
                .actualReleaseDate(actualReleaseDate)
                .custodialType(KeyValue.builder().code("CODE").description("DESCRIPTION").build())
                .length(2)
                .lengthUnits("Months")
                .licenceExpiryDate(licenceExpiryDate)
                .mainOffence(KeyValue.builder().description("Main Offence").build())
                .pssEndDate(pssEndDate)
                .sentenceDate(sentenceDate)
                .sentence(KeyValue.builder().description("Sentence Description").build())
                .build();

        CurrentOrderHeaderResponse orderHeaderResponse = mapper.buildCurrentOrderHeaderDetail(response);

        assertThat(orderHeaderResponse).isNotNull();
        assertThat(orderHeaderResponse.getSentenceId()).isEqualTo(1234);
        assertThat(orderHeaderResponse.getCustodialType().getCode()).isEqualTo("CODE");
        assertThat(orderHeaderResponse.getCustodialType().getDescription()).isEqualTo("DESCRIPTION");
        assertThat(orderHeaderResponse.getActualReleaseDate()).isEqualTo(actualReleaseDate);
        assertThat(orderHeaderResponse.getLength()).isEqualTo(2);
        assertThat(orderHeaderResponse.getLengthUnits()).isEqualTo("Months");
        assertThat(orderHeaderResponse.getLicenceExpiryDate()).isEqualTo(licenceExpiryDate);
        assertThat(orderHeaderResponse.getMainOffenceDescription()).isEqualTo("Main Offence");
        assertThat(orderHeaderResponse.getPssEndDate()).isEqualTo(pssEndDate);
        assertThat(orderHeaderResponse.getSentenceDate()).isEqualTo(sentenceDate);
        assertThat(orderHeaderResponse.getSentenceDescription()).isEqualTo("Sentence Description");
    }

    @DisplayName("Test custodial status mapping of nulls")
    @Test
    void shouldMapEmptyCustodialStatus() {
        CommunityApiCustodialStatusResponse response = CommunityApiCustodialStatusResponse.builder().build();
        CurrentOrderHeaderResponse orderHeaderResponse = mapper.buildCurrentOrderHeaderDetail(response);
        assertThat(orderHeaderResponse).isNotNull();
    }

    @DisplayName("Test mapping of an community API offender to offender match detail")
    @Test
    void shouldMapOffenderToMatchDetail() {
        OffenderMatchDetail offenderMatchDetail = mapper.offenderMatchDetailFrom(offenderResponse, "M");
        assertOffenderMatchFields(offenderMatchDetail);
    }

    @DisplayName("Test mapping of an offender to offender match detail with nulls on objects")
    @Test
    void shouldMapOffenderToMatchDetailWithNulls() {
        CommunityApiOffenderResponse offenderResponse = CommunityApiOffenderResponse.builder().title("Mr.").build();

        OffenderMatchDetail offenderMatchDetail = mapper.offenderMatchDetailFrom(offenderResponse, "M");

        assertThat(offenderMatchDetail.getTitle()).isEqualTo("Mr.");
        assertThat(offenderMatchDetail.getProbationStatus()).isSameAs(ProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(offenderMatchDetail.getAddress()).isNull();
        assertThat(offenderMatchDetail.getEvent()).isNull();
        assertThat(offenderMatchDetail.getMiddleNames()).hasSize(0);
    }

    @DisplayName("Test mapping of an offender match with additional fields for Event coming from the Sentence")
    @Test
    void shouldMapOffenderToMatchDetail_WithSentenceEvent() {

        LocalDate eventDate = LocalDate.of(2020, Month.JULY, 29);
        OffenderMatchDetail offenderMatch = mapper.offenderMatchDetailFrom(offenderResponse, "M");
        Sentence sentence = Sentence.builder()
            .description("Sentence description")
            .length(6)
            .lengthUnits("Months")
            .startDate(eventDate)
            .build();

        OffenderMatchDetail offenderMatchDetail = mapper.offenderMatchDetailFrom(offenderMatch, sentence);

        assertOffenderMatchFields(offenderMatchDetail);
        assertThat(offenderMatchDetail.getEvent().getLength()).isEqualTo(6);
        assertThat(offenderMatchDetail.getEvent().getLengthUnits()).isEqualTo("Months");
        assertThat(offenderMatchDetail.getEvent().getText()).isEqualTo("Sentence description");
        assertThat(offenderMatchDetail.getEvent().getStartDate()).isEqualTo(eventDate);
    }

    private void assertOffenderMatchFields(OffenderMatchDetail offenderMatchDetail) {
        assertThat(offenderMatchDetail.getForename()).isEqualTo("Aadland");
        assertThat(offenderMatchDetail.getMiddleNames()).hasSize(2);
        assertThat(offenderMatchDetail.getMiddleNames()).containsExactlyInAnyOrder("Felix", "Hope");
        assertThat(offenderMatchDetail.getSurname()).isEqualTo("Bertrand");
        assertThat(offenderMatchDetail.getDateOfBirth()).isEqualTo(LocalDate.of(2000, Month.JULY, 19));
        assertThat(offenderMatchDetail.getMatchIdentifiers().getCrn()).isEqualTo("X320741");
        assertThat(offenderMatchDetail.getMatchIdentifiers().getCro()).isEqualTo("123456/04A");
        assertThat(offenderMatchDetail.getMatchIdentifiers().getPnc()).isEqualTo("2004/0712343H");
        assertThat(offenderMatchDetail.getTitle()).isEqualTo("Mr.");
        assertThat(offenderMatchDetail.getAddress().getAddressNumber()).isEqualTo("19");
        assertThat(offenderMatchDetail.getAddress().getBuildingName()).isNull();
        assertThat(offenderMatchDetail.getAddress().getStreetName()).isEqualTo("Junction Road");
        assertThat(offenderMatchDetail.getAddress().getDistrict()).isEqualTo("Blackheath");
        assertThat(offenderMatchDetail.getAddress().getTown()).isEqualTo("Sheffield");
        assertThat(offenderMatchDetail.getAddress().getCounty()).isEqualTo("South Yorkshire");
        assertThat(offenderMatchDetail.getAddress().getPostcode()).isEqualTo("S10 2NA");

        assertThat(offenderMatchDetail.getProbationStatus()).isSameAs(ProbationStatus.CURRENT);
    }


}
