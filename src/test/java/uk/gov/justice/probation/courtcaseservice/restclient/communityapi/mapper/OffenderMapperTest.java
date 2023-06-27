package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchAlias;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetail;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCustody;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiSentence;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiSentenceStatusResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;
import uk.gov.justice.probation.courtcaseservice.service.model.SentenceStatus;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class OffenderMapperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String BASE_MOCK_PATH = "src/test/resources/mocks/__files/";

    private static CommunityApiOffenderResponse offenderResponse;

    @BeforeAll
    static void setUpBeforeClass() throws IOException {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());

        offenderResponse
            = OBJECT_MAPPER.readValue(new File(BASE_MOCK_PATH + "GET_offender_all_X320741.json"), CommunityApiOffenderResponse.class);
    }

    @Nested
    class ProbationStatusDetailTest {

        @DisplayName("Map probation status detail")
        @Test
        void whenMapProbationStatus_thenReturn() {
            var date = LocalDate.of(2020, Month.FEBRUARY, 1);

            var communityApiProbationStatus = CommunityApiProbationStatusDetail.builder()
                .status("NOT_SENTENCED")
                .preSentenceActivity(Boolean.TRUE)
                .inBreach(Boolean.TRUE)
                .previouslyKnownTerminationDate(date)
                .awaitingPsr(true)
                .build();

            var probationStatusDetail = OffenderMapper.probationStatusDetailFrom(communityApiProbationStatus);

            assertThat(probationStatusDetail.getStatus()).isEqualTo(DefendantProbationStatus.NOT_SENTENCED.name());
            assertThat(probationStatusDetail.getInBreach()).isTrue();
            assertThat(probationStatusDetail.isPreSentenceActivity()).isTrue();
            assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isEqualTo(date);
            assertThat(probationStatusDetail.getAwaitingPsr()).isTrue();
        }

        @DisplayName("Map probation status detail for unknown")
        @Test
        void givenNulls_whenMapProbationStatus_thenReturn() {
            var communityApiProbationStatus = CommunityApiProbationStatusDetail.builder()
                .status("NOT_SENTENCED")
                .preSentenceActivity(Boolean.FALSE)
                .awaitingPsr(false)
                .build();

            var probationStatusDetail = OffenderMapper.probationStatusDetailFrom(communityApiProbationStatus);

            assertThat(probationStatusDetail.getStatus()).isEqualTo("NOT_SENTENCED");
            assertThat(probationStatusDetail.getInBreach()).isNull();
            assertThat(probationStatusDetail.isPreSentenceActivity()).isFalse();
            assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isNull();
            assertThat(probationStatusDetail.getAwaitingPsr()).isFalse();
        }
    }

    @Nested
    class OffenderDetailTest {

        @DisplayName("Maps community API offender to OffenderDetail")
        @Test
        void shouldMapOffenderDetailsToOffenderDetail() {
            offenderResponse.setDateOfBirth(null);

            var offenderDetail = OffenderMapper.offenderDetailFrom(offenderResponse, ProbationStatusDetail.builder()
                    .status("CURRENT")
                    .build());

            assertThat(offenderDetail.getOtherIds().getCrn())
                .isNotNull()
                .isEqualTo("X320741");
            assertThat(offenderDetail.getTitle()).isEqualTo("Mr.");
            assertThat(offenderDetail.getOtherIds().getCrn()).isEqualTo("X320741");
            assertThat(offenderDetail.getOtherIds().getOffenderId()).isEqualTo(2500343964L);
            assertThat(offenderDetail.getOtherIds().getPncNumber()).isEqualTo("2004/0712343H");
            assertThat(offenderDetail.getOtherIds().getCroNumber()).isEqualTo("123456/04A");
            assertThat(offenderDetail.getMiddleNames()).containsExactlyInAnyOrder("Hope", "Felix");
            assertThat(offenderDetail.getProbationStatus()).isSameAs(DefendantProbationStatus.CURRENT);
            assertThat(offenderDetail.getDateOfBirth()).isNull();
            assertThat(offenderDetail.getForename()).isEqualTo("Aadland");
            assertThat(offenderDetail.getSurname()).isEqualTo("Bertrand");
        }
    }

    @Nested
    class ConvictionTest {

        @DisplayName("Maps convictions response to court case service conviction. Includes sentence and unpaid work.")
        @Test
        void shouldMapConvictionDetailsToConviction() throws IOException {

            var convictionsResponse
                = OBJECT_MAPPER
                .readValue(new File(BASE_MOCK_PATH + "offender-convictions/GET_offender_convictions_X320741.json"),
                    CommunityApiConvictionsResponse.class);

            var convictions = OffenderMapper.convictionsFrom(convictionsResponse);

            assertThat(convictions).hasSize(3);
            var conviction1 = convictions.get(0);
            assertThat(conviction1.getConvictionId()).isEqualTo("2500297061");
            assertThat(conviction1.getActive()).isEqualTo(false);
            assertThat(conviction1.getInBreach()).isTrue();
            assertThat(conviction1.isAwaitingPsr()).isFalse();
            assertThat(conviction1.getConvictionDate()).isEqualTo(LocalDate.of(2019, 9, 16));
            assertThat(conviction1.getCustodialType().getCode()).isEqualTo("B");
            assertThat(conviction1.getCustodialType().getDescription()).isEqualTo("Released - On Licence");

            assertThat(conviction1.getOffences()).hasSize(1);
            assertThat(conviction1.getOffences().get(0).getDescription()).isEqualTo("Assault on Police Officer - 10400");
            assertThat(conviction1.getOffences().get(0).isMain()).isTrue();
            assertThat(conviction1.getOffences().get(0).getOffenceDate()).isEqualTo(LocalDate.of(2019, 9, 9));

            assertThat(conviction1.getSentence().getSentenceId()).isEqualTo("123456");
            assertThat(conviction1.getSentence().getDescription()).isEqualTo("Absolute/Conditional Discharge");
            assertThat(conviction1.getSentence().getLength()).isEqualTo(0);
            assertThat(conviction1.getSentence().getLengthUnits()).isEqualTo("Months");
            assertThat(conviction1.getSentence().getLengthInDays()).isEqualTo(0);
            assertThat(conviction1.getSentence().getTerminationDate()).isEqualTo(LocalDate.of(2020, 1, 1));
            assertThat(conviction1.getSentence().getTerminationReason()).isEqualTo("Auto Terminated");

            assertThat(conviction1.getSentence().getUnpaidWork().getMinutesOffered()).isEqualTo(480);
            assertThat(conviction1.getSentence().getUnpaidWork().getMinutesCompleted()).isEqualTo(60);
            assertThat(conviction1.getSentence().getUnpaidWork().getAppointmentsToDate()).isEqualTo(12);
            assertThat(conviction1.getSentence().getUnpaidWork().getAttended()).isEqualTo(2);
            assertThat(conviction1.getSentence().getUnpaidWork().getAcceptableAbsences()).isEqualTo(2);
            assertThat(conviction1.getSentence().getUnpaidWork().getUnacceptableAbsences()).isEqualTo(1);

            // conviction date + sentence.lengthInDays (startDate not present so null)
            assertThat(conviction1.getSentence().getEndDate()).isNull();
            assertThat(conviction1.getEndDate()).isNull();

            var conviction2 = convictions.get(1);
            assertThat(conviction2.getConvictionId()).isEqualTo("2500295345");
            assertThat(conviction2.getInBreach()).isTrue();
            assertThat(conviction2.isAwaitingPsr()).isTrue();
            assertThat(conviction2.getCustodialType().getCode()).isEqualTo("B");
            assertThat(conviction2.getSentence().getSentenceId()).isEqualTo("123457");
            assertThat(conviction2.getSentence().getDescription()).isEqualTo("CJA - Indeterminate Public Prot.");
            assertThat(conviction2.getSentence().getTerminationDate()).isEqualTo(LocalDate.of(2019, 1, 1));
            assertThat(conviction2.getSentence().getTerminationReason()).isEqualTo("ICMS Miscellaneous Event");
            assertThat(conviction2.getSentence().getLengthInDays()).isEqualTo(1826);
            assertThat(conviction2.getSentence().getUnpaidWork()).isNull();
            assertThat(conviction2.getSentence().getStartDate()).isEqualTo(LocalDate.of(2014, 1, 1));

            // conviction date + sentence.lengthInDays
            assertThat(conviction2.getSentence().getEndDate()).isEqualTo(LocalDate.of(2014, 1, 1).plusDays(1826));
            assertThat(conviction2.getEndDate()).isEqualTo(LocalDate.of(2014, 1, 1).plusDays(1826));

            var conviction3 = convictions.get(2);
            assertThat(conviction3.getConvictionId()).isEqualTo("2500295343");
            assertThat(conviction3.getEndDate()).isNull();
            assertThat(conviction3.getSentence()).isNull();
        }

        @DisplayName("Maps convictions response to court case service conviction. Null offence list and all nullable fields null.")
        @Test
        void shouldMapConvictionDetailsToConvictionNull() {

            var convictionResponse = CommunityApiConvictionResponse
                .builder()
                .convictionId("123")
                .offences(null)
                .build();
            var convictionsResponse = new CommunityApiConvictionsResponse(singletonList(convictionResponse));

            var convictions = OffenderMapper.convictionsFrom(convictionsResponse);

            var expectedConviction = Conviction.builder()
                .active(null)
                .convictionDate(null)
                .sentence(null)
                .endDate(null)
                .convictionId("123")
                .offences(Collections.emptyList())
                .build();

            assertThat(convictions).hasSize(1);
            assertThat(convictions.get(0)).usingRecursiveComparison().isEqualTo(expectedConviction);
        }

        @DisplayName("Maps convictions response to court case service conviction.")
        @Test
        void shouldMapConvictionDetailsToConvictionSentenceSetNullUnpaidWordNullCustodialType() {

            var convictionResponse = CommunityApiConvictionResponse
                .builder()
                .convictionId("123")
                .custody(CommunityApiCustody.builder().build())
                .offences(Collections.emptyList())
                .build();
            var convictionsResponse = new CommunityApiConvictionsResponse(singletonList(convictionResponse));

            var convictions = OffenderMapper.convictionsFrom(convictionsResponse);

            var expectedConviction = Conviction.builder()
                .active(null)
                .awaitingPsr(false)
                .convictionDate(null)
                .sentence(null)
                .endDate(null)
                .convictionId("123")
                .offences(Collections.emptyList())
                .build();

            assertThat(convictions).hasSize(1);
            assertThat(convictions.get(0)).usingRecursiveComparison().isEqualTo(expectedConviction);
        }

        @DisplayName("No end date if sentence is null")
        @Test
        void endDateCalculatorNullSentence() {
            assertThat(OffenderMapper.endDateCalculator(null)).isNull();
        }

        @DisplayName("No end date if length of sentence is null")
        @Test
        void endDateCalculatorNullSentenceLength() {
            final CommunityApiSentence sentence = CommunityApiSentence.builder()
                .startDate(LocalDate.of(2019, 10, 1))
                .build();
            assertThat(OffenderMapper.endDateCalculator(sentence)).isNull();
        }

        @DisplayName("No end date if convictionDate is null")
        @Test
        void endDateCalculatorNullDate() {
            final CommunityApiSentence sentence = CommunityApiSentence.builder().lengthInDays(1).build();
            assertThat(OffenderMapper.endDateCalculator(sentence)).isNull();
        }

        @DisplayName("No end date if convictionDate is null")
        @Test
        void endDateCalculatorNormal() {
            final CommunityApiSentence sentence = CommunityApiSentence.builder().lengthInDays(1)
                .startDate(LocalDate.of(2019, 10, 1))
                .build();
            assertThat(OffenderMapper.endDateCalculator(sentence)).isEqualTo(LocalDate.of(2019, 10, 2));
        }
    }

    @Nested
    class CustodialStatusTest {

        @DisplayName("Test custodial status mapping of values")
        @Test
        void shouldMapFullCustodialStatus() {
            LocalDate actualReleaseDate = LocalDate.of(2020, 8, 5);
            LocalDate licenceExpiryDate = LocalDate.of(2020, 8, 6);
            LocalDate pssEndDate = LocalDate.of(2020, 8, 7);
            LocalDate sentenceDate = LocalDate.of(2020, 8, 8);
            CommunityApiSentenceStatusResponse response = CommunityApiSentenceStatusResponse.builder()
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

            var sentenceStatusResponse = OffenderMapper.buildSentenceStatus(response);

            assertThat(sentenceStatusResponse).isNotNull();
            assertThat(sentenceStatusResponse.getSentenceId()).isEqualTo(1234);
            assertThat(sentenceStatusResponse.getCustodialType().getCode()).isEqualTo("CODE");
            assertThat(sentenceStatusResponse.getCustodialType().getDescription()).isEqualTo("DESCRIPTION");
            assertThat(sentenceStatusResponse.getActualReleaseDate()).isEqualTo(actualReleaseDate);
            assertThat(sentenceStatusResponse.getLength()).isEqualTo(2);
            assertThat(sentenceStatusResponse.getLengthUnits()).isEqualTo("Months");
            assertThat(sentenceStatusResponse.getLicenceExpiryDate()).isEqualTo(licenceExpiryDate);
            assertThat(sentenceStatusResponse.getMainOffenceDescription()).isEqualTo("Main Offence");
            assertThat(sentenceStatusResponse.getPssEndDate()).isEqualTo(pssEndDate);
            assertThat(sentenceStatusResponse.getSentenceDate()).isEqualTo(sentenceDate);
            assertThat(sentenceStatusResponse.getSentenceDescription()).isEqualTo("Sentence Description");
        }

        @DisplayName("Test custodial status mapping of nulls")
        @Test
        void shouldMapEmptyCustodialStatus() {
            CommunityApiSentenceStatusResponse response = CommunityApiSentenceStatusResponse.builder().build();
            SentenceStatus sentenceStatusResponse = OffenderMapper.buildSentenceStatus(response);
            assertThat(sentenceStatusResponse).isNotNull();
        }

    }

    @Nested
    class OffenderMatchDetailTest {

        @DisplayName("Test mapping of an community API offender to offender match detail")
        @Test
        void shouldMapOffenderToMatchDetail() {
            OffenderMatchDetail offenderMatchDetail = OffenderMapper.offenderMatchDetailFrom(offenderResponse, "M");

            assertThat(offenderMatchDetail.getProbationStatus()).isNull();
            assertOffenderMatchFields(offenderMatchDetail);
        }

        @DisplayName("Test mapping of an offender to offender match detail with nulls on objects")
        @Test
        void shouldMapOffenderToMatchDetailWithNulls() {
            CommunityApiOffenderResponse offenderResponse = CommunityApiOffenderResponse.builder().title("Mr.").build();

            OffenderMatchDetail offenderMatchDetail = OffenderMapper.offenderMatchDetailFrom(offenderResponse, "M");

            assertThat(offenderMatchDetail.getTitle()).isEqualTo("Mr.");
            assertThat(offenderMatchDetail.getProbationStatus()).isNull();
            assertThat(offenderMatchDetail.getAddress()).isNull();
            assertThat(offenderMatchDetail.getEvent()).isNull();
            assertThat(offenderMatchDetail.getMiddleNames()).hasSize(0);
        }

        @DisplayName("Test mapping of an offender match with additional fields for Event coming from the Sentence")
        @Test
        void shouldMapOffenderToMatchDetail_WithSentenceEventAndProbationStatus() {

            LocalDate eventDate = LocalDate.of(2020, Month.JULY, 29);
            OffenderMatchDetail offenderMatch = OffenderMapper.offenderMatchDetailFrom(offenderResponse, "M");
            Sentence sentence = Sentence.builder()
                .description("Sentence description")
                .length(6)
                .lengthUnits("Months")
                .startDate(eventDate)
                .build();

            final var probationStatus = ProbationStatusDetail.builder()
                    .status("CURRENT")
                    .build();
            OffenderMatchDetail offenderMatchDetail = OffenderMapper.offenderMatchDetailFrom(offenderMatch, sentence, probationStatus, 0.9);

            assertOffenderMatchFields(offenderMatchDetail);

            assertThat(offenderMatchDetail.getProbationStatus()).isSameAs(DefendantProbationStatus.CURRENT);
            assertThat(offenderMatchDetail.getEvent().getLength()).isEqualTo(6);
            assertThat(offenderMatchDetail.getEvent().getLengthUnits()).isEqualTo("Months");
            assertThat(offenderMatchDetail.getEvent().getText()).isEqualTo("Sentence description");
            assertThat(offenderMatchDetail.getEvent().getStartDate()).isEqualTo(eventDate);
            assertThat(offenderMatchDetail.getMatchProbability()).isEqualTo(0.9);
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

            OffenderMatchAlias offenderMatchAlias1 = offenderMatchDetail.getMatchIdentifiers().getAliases().get(0);
            assertThat(offenderMatchAlias1.getDateOfBirth()).isEqualTo(LocalDate.of(2022, 5, 17));
            assertThat(offenderMatchAlias1.getFirstName()).isEqualTo("Aliasone");
            assertThat(offenderMatchAlias1.getSurname()).isEqualTo("Bertrand");
            assertThat(offenderMatchAlias1.getGender()).isEqualTo("Male");
            assertThat(offenderMatchAlias1.getMiddleNames()).isEqualTo(List.of("Midone", "Midtwo"));

            OffenderMatchAlias offenderMatchAlias2 = offenderMatchDetail.getMatchIdentifiers().getAliases().get(1);
            assertThat(offenderMatchAlias1.getDateOfBirth()).isEqualTo(LocalDate.of(2022, 5, 17));
            assertThat(offenderMatchAlias2.getFirstName()).isEqualTo("Aliastwo");
            assertThat(offenderMatchAlias2.getSurname()).isEqualTo("Bertrand");
            assertThat(offenderMatchAlias1.getGender()).isEqualTo("Male");
            assertThat(offenderMatchAlias2.getMiddleNames()).isEqualTo(List.of("Midthree"));
        }
    }

}
