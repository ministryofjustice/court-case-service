package uk.gov.justice.probation.courtcaseservice.controller;


import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.Address;
import uk.gov.justice.probation.courtcaseservice.controller.model.Event;
import uk.gov.justice.probation.courtcaseservice.controller.model.MatchIdentifiers;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetail;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetailResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ExtendWith(MockitoExtension.class)
class OffenderMatchesControllerTest {
    public static final String COURT_CODE = "B01CX00";
    private static final String CASE_NO = "1234567890";
    private static final String CASE_ID = "cb2199b0-5a3e-4fea-858d-af23c998ac3d";
    private static final String DEFENDANT_ID = "1081ca4e-8aa4-42ec-8212-530dec781e56";
    private static final String GROUP_OFFENDER_MATCH_PATH = "/court/" + COURT_CODE + "/case/" + CASE_NO + "/grouped-offender-matches/";
    private static final String CASE_ID_GROUP_OFFENDER_MATCH_PATH = "/case/" + CASE_ID + "/defendant/" + DEFENDANT_ID + "/grouped-offender-matches/";
    protected static final String OFFENDER_MATCHES_DETAIL_PATH = "/court/%s/case/%s/matchesDetail";
    protected static final String OFFENDER_MATCHES_DEFENDANT_DETAIL_PATH = "/case/%s/defendant/%s/matchesDetail";

    private WebTestClient webTestClient;

    @Mock
    private OffenderMatchService offenderMatchService;
    @Mock
    private GroupedOffenderMatchesEntity entity;

    @BeforeEach
    void setUp() {
        var controller = new OffenderMatchesController(offenderMatchService);
        this.webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void givenSuccessfulCreate_thenReturnLocationHeader() {
        when(offenderMatchService.createOrUpdateGroupedMatches(eq(COURT_CODE), eq(CASE_NO), any())).thenReturn(Mono.just(entity));
        var expectedGroupId = 1111L;
        when(entity.getId()).thenReturn(expectedGroupId);
        webTestClient.post()
                .uri(GROUP_OFFENDER_MATCH_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue("{\n" +
                "    \"matches\": [\n" +
                "        {\n" +
                "                \"matchIdentifiers\": {\n" +
                "                \"crn\": \"X346204\"\n" +
                "            },\n" +
                "            \"matchType\": \"NAME_DOB\",\n" +
                "            \"confirmed\": \"true\",\n" +
                "            \"rejected\": \"false\"\n" +
                "        }\n" +
                "    ]\n" +
                "}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().value("Location", equalTo(GROUP_OFFENDER_MATCH_PATH + expectedGroupId));
    }

    @Test
    void whenCreateByCaseId_thenReturnLocationHeader() {
        when(offenderMatchService.createOrUpdateGroupedMatchesByDefendant(eq(CASE_ID), eq(DEFENDANT_ID), any())).thenReturn(Mono.just(entity));
        Long expectedGroupId = 1111L;
        when(entity.getId()).thenReturn(expectedGroupId);
        webTestClient.post()
            .uri(CASE_ID_GROUP_OFFENDER_MATCH_PATH)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .bodyValue("{\n" +
                "    \"matches\": [\n" +
                "        {\n" +
                "                \"matchIdentifiers\": {\n" +
                "                \"crn\": \"X346204\"\n" +
                "            },\n" +
                "            \"matchType\": \"NAME_DOB\",\n" +
                "            \"confirmed\": \"true\",\n" +
                "            \"rejected\": \"false\"\n" +
                "        }\n" +
                "    ]\n" +
                "}")
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().value("Location", equalTo(CASE_ID_GROUP_OFFENDER_MATCH_PATH + expectedGroupId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/court/FOO/case/1234567890/grouped-offender-matches/",
        "/case/f1e1867f-94a5-45a2-81cf-92780a51564d/defendant/f1e1867f-94a5-45a2-81cf-92780a31364d/grouped-offender-matches/"})
    void givenEmptyJsonBody_whenPostMadeToOffenderMatches_thenReturnBadRequest(String path) {
        String body = "{}";
        assertBadRequestForBody(body, path);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/court/FOO/case/1234567890/grouped-offender-matches/",
        "/case/f1e1867f-94a5-45a2-81cf-92780a51564d/defendant/f1e1867f-94a5-45a2-81cf-92780a31364d/grouped-offender-matches/"})
    void givenMissingMatchIdentifiers_whenPostMadeToOffenderMatches_thenReturnBadRequest(String path) {
        assertBadRequestForBody("{\n" +
                "    \"matches\": [\n" +
                "        {\n" +
                "            \"matchType\": \"NAME_DOB\",\n" +
                "            \"confirmed\": \"true\"\n" +
                "        }\n" +
                "    ]\n" +
                "}", path);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/court/FOO/case/1234567890/grouped-offender-matches/",
        "/case/f1e1867f-94a5-45a2-81cf-92780a51564d/defendant/f1e1867f-94a5-45a2-81cf-92780a31364d/grouped-offender-matches/"})
    void givenMissingCrn_whenPostMadeToOffenderMatches_thenReturnBadRequest(String path) {
        assertBadRequestForBody("{\n" +
                        "    \"matches\": [\n" +
                        "        {\n" +
                        "                \"matchIdentifiers\": {\n" +
                        "                \"pnc\": \"pnc123\"\n" +
                        "            },\n" +
                        "            \"matchType\": \"NAME_DOB\",\n" +
                        "            \"confirmed\": \"true\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", path);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/court/FOO/case/1234567890/grouped-offender-matches/",
        "/case/f1e1867f-94a5-45a2-81cf-92780a51564d/defendant/f1e1867f-94a5-45a2-81cf-92780a31364d/grouped-offender-matches/"})
    void givenMissingMatchType_whenPostMadeToOffenderMatches_thenReturnBadRequest(String path) {
        assertBadRequestForBody("{\n" +
                        "    \"matches\": [\n" +
                        "        {\n" +
                        "                \"matchIdentifiers\": {\n" +
                        "                \"crn\": \"X346204\"\n" +
                        "            },\n" +
                        "            \"confirmed\": \"true\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", path);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/court/FOO/case/1234567890/grouped-offender-matches/",
        "/case/f1e1867f-94a5-45a2-81cf-92780a51564d/defendant/f1e1867f-94a5-45a2-81cf-92780a31364d/grouped-offender-matches/"})
    void givenMissingConfirmedFlag_whenPostMadeToOffenderMatches_thenReturnBadRequest(String path) {
        assertBadRequestForBody("{\n" +
                        "    \"matches\": [\n" +
                        "        {\n" +
                        "                \"matchIdentifiers\": {\n" +
                        "                \"crn\": \"X346204\"\n" +
                        "            },\n" +
                        "            \"matchType\": \"NAME_DOB\",\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", path);
    }

    @Test
    void givenMultipleMatches_whenGetOffenderMatchDetail_thenReturnMultiple() {

        var detail1 = buildOffenderMatchDetail("Christopher", ProbationStatus.PREVIOUSLY_KNOWN);
        var detail2 = buildOffenderMatchDetail("Christian", ProbationStatus.CURRENT);

        var response = OffenderMatchDetailResponse.builder()
                                                                            .offenderMatchDetails(List.of(detail1, detail2))
                                                                            .build();

        when(offenderMatchService.getOffenderMatchDetails(COURT_CODE, CASE_NO)).thenReturn(response);

        final var body = webTestClient.get()
            .uri(String.format(OFFENDER_MATCHES_DETAIL_PATH, COURT_CODE, CASE_NO))
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody();

        verify(offenderMatchService).getOffenderMatchDetails(COURT_CODE, CASE_NO);
        verifyNoMoreInteractions(offenderMatchService);
        validateBody(body);
    }

    @Test
    void givenMultipleMatches_whenGetOffenderMatchDetailByCaseAndDefendantId_thenReturnMultiple() {

        var detail1 = buildOffenderMatchDetail("Christopher", ProbationStatus.PREVIOUSLY_KNOWN);
        var detail2 = buildOffenderMatchDetail("Christian", ProbationStatus.CURRENT);

        var response = OffenderMatchDetailResponse.builder()
            .offenderMatchDetails(List.of(detail1, detail2))
            .build();

        when(offenderMatchService.getOffenderMatchDetailsByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(response);

        final var body = webTestClient.get()
            .uri(String.format(OFFENDER_MATCHES_DEFENDANT_DETAIL_PATH, CASE_ID, DEFENDANT_ID))
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody();

        verify(offenderMatchService).getOffenderMatchDetailsByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
        verifyNoMoreInteractions(offenderMatchService);
        validateBody(body);
    }

    private void validateBody(BodyContentSpec body) {
        body.jsonPath("offenderMatchDetails[0].forename").isEqualTo("Christopher")
            .jsonPath("offenderMatchDetails[0].surname").isEqualTo("Bailey")
            .jsonPath("offenderMatchDetails[0].middleNames").isArray()
            .jsonPath("offenderMatchDetails[0].dateOfBirth").isEqualTo("1969-08-26")
            .jsonPath("offenderMatchDetails[0].matchIdentifiers.crn").isEqualTo("C178657")
            .jsonPath("offenderMatchDetails[0].matchIdentifiers.cro").isEqualTo("CRO1232")
            .jsonPath("offenderMatchDetails[0].probationStatus").isEqualTo("Previously known")
            .jsonPath("offenderMatchDetails[0].address.buildingName").isEqualTo("Dunroamin")
            .jsonPath("offenderMatchDetails[0].address.county").isEqualTo("Leicestershire")
            .jsonPath("offenderMatchDetails[0].address.postcode").isEqualTo("LE2 1TG")
            .jsonPath("offenderMatchDetails[0].mostRecentEvent.startDate").isEqualTo("2017-11-27")
            .jsonPath("offenderMatchDetails[0].mostRecentEvent.text").isEqualTo("CJA Standard Determinate Custody")
            .jsonPath("offenderMatchDetails[0].mostRecentEvent.length").isEqualTo("10")
            .jsonPath("offenderMatchDetails[0].mostRecentEvent.lengthUnits").isEqualTo("Months")

            .jsonPath("offenderMatchDetails[1].forename").isEqualTo("Christian")
            .jsonPath("offenderMatchDetails[1].probationStatus").isEqualTo("Current");
    }

    private OffenderMatchDetail buildOffenderMatchDetail(String forename, ProbationStatus probationStatus) {
        return OffenderMatchDetail.builder()
                .dateOfBirth(LocalDate.of(1969, Month.AUGUST, 26))
                .title("Mr.")
                .forename(forename)
                .middleNames(List.of("Paul"))
                .surname("Bailey")
                .matchIdentifiers(MatchIdentifiers.builder().crn("C178657").cro("CRO1232").build())
                .probationStatus(probationStatus)
                .address(Address.builder().buildingName("Dunroamin").county("Leicestershire")
                    .postcode("LE2 1TG").build())
                .event(Event.builder().startDate(LocalDate.of(2017, Month.NOVEMBER, 27))
                    .text("CJA Standard Determinate Custody")
                    .length(10)
                    .lengthUnits("Months").build())
                .build();
    }

    private void assertBadRequestForBody(String body, String path) {
        webTestClient.post()
                .uri(path)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
