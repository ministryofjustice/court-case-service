package uk.gov.justice.probation.courtlistservice.prototype.controller;

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
import uk.gov.justice.probation.courtlistservice.prototype.data.api.CourtList;

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

    private String longLastingToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJhbmR5Lm1hcmtlIiwic2NvcGUiOlsid3JpdGUiXSwiYXV0aF9zb3VyY2UiOiJkZWxpdXMiLCJleHAiOjE4Nzk1MTI4MjgsImF1dGhvcml0aWVzIjpbIlJPTEVfQVBCVDAwNCIsIlJPTEVfTklCVDAwMDIiLCJST0xFX09JQlQwMDA0IiwiUk9MRV9OSUJUMDAwMyIsIlJPTEVfT0lCVDAwMDMiLCJST0xFX0NBQlQwMDgiLCJST0xFX0FQQlQwMDUiLCJST0xFX0NBQlQwMDUiLCJST0xFX0FQQlQwMDIiLCJST0xFX09JQlQwMDA1IiwiUk9MRV9BUEJUMDAzIiwiUk9MRV9DQUJUMDAzIiwiUk9MRV9DQUJUMDA0IiwiUk9MRV9DQUJUMDAxIiwiUk9MRV9DQUJUMDAyIiwiUk9MRV9DTEJUMDAxIiwiUk9MRV9DTEJUMDAyIiwiUk9MRV9DTEJUMDAzIiwiUk9MRV9SUEJUMDAxIiwiUk9MRV9BUEJUMDAxIiwiUk9MRV9SUEJUMDAzIiwiUk9MRV9PSUJUMDAwMiIsIlJPTEVfTklCVDAwMDEiLCJST0xFX09JQlQwMDAxIiwiUk9MRV9DTEJUMDA1IiwiUk9MRV9DTEJUMDA2IiwiUk9MRV9DTEJUMDA3IiwiUk9MRV9VUEJUMDAxIiwiUk9MRV9SUkJUMDAzIiwiUk9MRV9SUkJUMDAxIiwiUk9MRV9EUkJUMDA3IiwiUk9MRV9SUkJUMDAyIiwiUk9MRV9EUkJUMDA2IiwiUk9MRV9DV0JUMDAyIiwiUk9MRV9DV0JUMDAxIiwiUk9MRV9EUkJUMDA1IiwiUk9MRV9EUkJUMDA0IiwiUk9MRV9EUkJUMDAzIiwiUk9MRV9EUkJUMDAyIiwiUk9MRV9EUkJUMDAxIiwiUk9MRV9UQ0JUMDAxIiwiUk9MRV9UQ0JUMDA0IiwiUk9MRV9UQ0JUMDAyIiwiUk9MRV9UQ0JUMDAzIiwiUk9MRV9SREJUMDAzIiwiUk9MRV9SREJUMDAxIiwiUk9MRV9SREJUMDAyIiwiUk9MRV9DREJUMDAxIiwiUk9MRV9FWEJUMDAxIiwiUk9MRV9HVEJUMDAyIiwiUk9MRV9XUEJUMDAxIiwiUk9MRV9XUEJUMDAyIiwiUk9MRV9XUEJUMDAzIiwiUk9MRV9VQUJUMDA1MCIsIlJPTEVfT0FCVDAwMiIsIlJPTEVfVUFCVDAwNTEiLCJST0xFX09BQlQwMDEiLCJST0xFX0VYQlQwMDIiLCJST0xFX0VYQlQwMDMiLCJST0xFX09JQlQwNTAiLCJST0xFX0NBQlQwMTIiLCJST0xFX0NBQlQwMTAiLCJST0xFX0NBQlQwMTEiLCJST0xFX0NXQlQxMDEiLCJST0xFX1VXQlQwMTAiLCJST0xFX0NXQlQxMDMiLCJST0xFX0NXQlQxMDIiLCJST0xFX0RNQlQwMDMiLCJST0xFX0RNQlQwMDIiLCJST0xFX0RNQlQwMDEiLCJST0xFX1dQQlQwMDQiLCJST0xFX0RTQlQwMDIiLCJST0xFX1dQQlQwMDUiLCJST0xFX1dQQlQwMDYiLCJST0xFX0RQQlQwMDEiLCJST0xFX0RTQlQwMDEiLCJST0xFX0NBQlQwMDkiLCJST0xFX1VXQlQwMDMiLCJST0xFX1NFQlQwMTMiLCJST0xFX1NFQlQwMTIiLCJST0xFX1NFQlQwMTEiLCJST0xFX1NQQlQwMDMiLCJST0xFX1NQQlQwMDIiLCJST0xFX1NQQlQwMDEiLCJST0xFX0NMQlQwNTAiLCJST0xFX1NZQlQwMDMiLCJST0xFX1JQQlQwNTAiLCJST0xFX1NZQlQwMDEiLCJST0xFX1NZQlQwMDIiLCJST0xFX0FQQlQwNTAiLCJST0xFX1NFQlQwMDIiLCJST0xFX1NFQlQwMDEiLCJST0xFX1NFQlQwMDMiLCJST0xFX0NXQlQwNTAiLCJST0xFX1NQR0FEQlQwMDEiLCJST0xFX0NMQlQxMDEiLCJST0xFX1VXQlQwNjAiLCJST0xFX0VYQlQwNTAiLCJST0xFX1VNQlQwMDEiLCJST0xFX1VBQlQwMDA1IiwiUk9MRV9OU0JUMDA4IiwiUk9MRV9XUEJUMDUwIiwiUk9MRV9VQUJUMDAwMyIsIlJPTEVfVUFCVDAwMDEiLCJST0xFX1VBQlQwMDAyIiwiUk9MRV9OU0JUMDAyIiwiUk9MRV9VV0JUMDUyIiwiUk9MRV9OU0JUMDAxIiwiUk9MRV9OU0JUMDA2IiwiUk9MRV9OU0JUMDA3IiwiUk9MRV9OU0JUMDA0IiwiUk9MRV9VV0JUMDUxIl0sImp0aSI6IjgyMmE2MDM2LWE0ZDgtNDZhMC04MjdmLWM3MWE2YTk1ZWExZCIsImNsaWVudF9pZCI6InByb2JhdGlvbl9pbl9jb3VydCJ9.O06sXnXG0DOMaH8jvYOeCzvhWcq4irLWrkufUbyHKItVeMApIG2qQ4Q_hEe1rqTJLnR0y8NjaBCbA_q7RYFrxp64_A5KuYq3Zz18QEKbD6a2cvsBxm6aB4BLYjBi3bFHlPnA5KLxDp6ktDM2p4DlGG6H91NbdQOZzZ-BWuy7yrDeuRwmI3zPsiRPJirpmTJ5sIn9rW_SX1IR6_q5TATeiw7DGhYM-NWN99ItjopsvWwElEF1DPE3yB3KomdM8mZ91UWXVAAhNqsExICLgYrdM7nlxcsiTkDgp8KWmMGG9lZO73O0o9AlkEPRic1M3v-Gv1bzCIKziFjbGlQzIgZPlA";

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
                .header("Authorization", "Bearer " + longLastingToken)
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
                .header("Authorization", "Bearer " + longLastingToken)
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