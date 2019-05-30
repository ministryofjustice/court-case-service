package uk.gov.justice.digital.probation.court.list.courtlistservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.digital.probation.court.list.courtlistservice.data.api.CourtList;

import java.io.IOException;
import java.nio.charset.Charset;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class CourtListControllerTest {
    @LocalServerPort
    int port;

    @Autowired
    private ObjectMapper objectMapper;


    @Rule
    public WireMockRule wireMock = new WireMockRule(wireMockConfig().port(8081).jettyStopTimeout(10000L));

    @Before
    public void before() {
        RestAssured.port = port;
        RestAssured.basePath = "/court";
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (aClass, s) -> objectMapper
        ));

    }

    @Test
    public void returnsAllSessionsReturnedFromCrimePortal() throws IOException {
        wireMock.stubFor(
                get(urlPathEqualTo("/court/list"))
                        .willReturn(
                                okForContentType("application/xml",  loadResource("/CourtList.xml"))));

        CourtList courtList = given()
                .when()
                .get("list")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CourtList.class);

        assertThat(courtList.getSessions()).hasSize(2);
    }

    @Test
    public void returnsAllSessionsReturnedFromCrimePortalForDate() throws IOException {
        wireMock.stubFor(
                get(urlPathEqualTo("/court/Sheffield/list"))
                        .withQueryParam("date", equalTo("2019-07-19"))
                        .willReturn(
                                okForContentType("application/xml",  loadResource("/CourtList.xml"))));

        CourtList courtList = given()
                .when()
                .param("date", "2019-07-19")
                .get("Sheffield/list")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(CourtList.class);

        assertThat(courtList.getSessions()).hasSize(2);
    }

    private String loadResource(String resource) throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream(resource), Charset.defaultCharset());
    }


}