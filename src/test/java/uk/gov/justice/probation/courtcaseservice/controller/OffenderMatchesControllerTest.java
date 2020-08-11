package uk.gov.justice.probation.courtcaseservice.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ExtendWith(MockitoExtension.class)
public class OffenderMatchesControllerTest {
    public static final String COURT_CODE = "SHF";
    private WebTestClient webTestClient;

    private OffenderMatchesController controller;
    @Mock
    private OffenderMatchService offenderMatchService;
    @Mock
    private GroupedOffenderMatchesEntity entity;

    @BeforeEach
    public void setUp() {
        controller = new OffenderMatchesController(offenderMatchService);
        this.webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    public void givenSuccessfulCreate_thenReturnLocationHeader() {
        when(offenderMatchService.createGroupedMatches(eq("SHF"), eq("1234567890"), any())).thenReturn(Mono.just(entity));
        Long expectedGroupId = 1111L;
        when(entity.getId()).thenReturn(expectedGroupId);
        webTestClient.post()
                .uri("/court/SHF/case/1234567890/grouped-offender-matches/")
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
                .expectHeader().value("Location", equalTo("/court/SHF/case/1234567890/grouped-offender-matches/" + expectedGroupId));
    }

    @Test
    public void givenEmptyJsonBody_whenPostMadeToOffenderMatches_thenReturnBadRequest() {
        String body = "{}";
        assertBadRequestForBody(body);
    }

    @Test
    public void givenMissingMatchIdentifiers_whenPostMadeToOffenderMatches_thenReturnBadRequest() {
        assertBadRequestForBody("{\n" +
                "    \"matches\": [\n" +
                "        {\n" +
                "            \"matchType\": \"NAME_DOB\",\n" +
                "            \"confirmed\": \"true\"\n" +
                "        }\n" +
                "    ]\n" +
                "}");
    }

    @Test
    public void givenMissingCrn_whenPostMadeToOffenderMatches_thenReturnBadRequest() {
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
                        "}");
    }

    @Test
    public void givenMissingMatchType_whenPostMadeToOffenderMatches_thenReturnBadRequest() {
        assertBadRequestForBody("{\n" +
                        "    \"matches\": [\n" +
                        "        {\n" +
                        "                \"matchIdentifiers\": {\n" +
                        "                \"crn\": \"X346204\"\n" +
                        "            },\n" +
                        "            \"confirmed\": \"true\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
    }

    @Test
    public void givenMissingConfirmedFlag_whenPostMadeToOffenderMatches_thenReturnBadRequest() {
        assertBadRequestForBody("{\n" +
                        "    \"matches\": [\n" +
                        "        {\n" +
                        "                \"matchIdentifiers\": {\n" +
                        "                \"crn\": \"X346204\"\n" +
                        "            },\n" +
                        "            \"matchType\": \"NAME_DOB\",\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
    }

    private void assertBadRequestForBody(String body) {
        webTestClient.post()
                .uri("/court/FOO/case/1234567890/grouped-offender-matches/")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
