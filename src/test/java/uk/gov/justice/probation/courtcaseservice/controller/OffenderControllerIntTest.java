package uk.gov.justice.probation.courtcaseservice.controller;


import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.TestConfig;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OffenderControllerIntTest {

    @LocalServerPort
    private int port;

    @Before
    public void setUp() {
        TestConfig.configureRestAssuredForIntTest(port);
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .port(8090)
            .usingFilesUnderClasspath("mocks"));

    @Test
    public void givenOffenderDoesNotExist_whenCallMadeToGetOffenderData_thenReturnNotFound() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/offender/NOT_THERE/probation-record")
                .then()
                .statusCode(404);
    }

    @Test
    public void whenCallMadeToGetOffenderData_thenReturnCorrectData() {
        given()
                .accept(ContentType.JSON)
        .when()
                .get("/offender/X320741/probation-record")
        .then()
                .statusCode(200)
                .body("crn",  equalTo("X320741"))
                .body("offenderManagers[0].forenames", equalTo("Temperance"))
                .body("offenderManagers[0].surname", equalTo("Brennan"))
                .body("offenderManagers[0].allocatedDate", equalTo(standardDateOf(2019, 9, 30)))

        // TODO: Get convictions
//                .body("convictions[0].convictionId", equalTo("2500297061"))
//                .body("convictions[0].active", equalTo(false))
//                .body("convictions[0].offences[0].description", equalTo("Assault on Police Officer - 10400"))
//                .body("convictions[0].sentence.description", equalTo("Absolute/Conditional Discharge"))
//                .body("convictions[0].sentence.length", equalTo(0))
//                .body("convictions[0].sentence.lengthUnits", equalTo("Months"))
//                .body("convictions[0].sentence.lengthInDays", equalTo(0))
//                .body("convictions[0].sentence.convictionDate", equalTo(standardDateOf(2019, 9,16)))
//
//                .body("convictions[1].convictionId", equalTo("2500295345"))
//                .body("convictions[1].active", equalTo(true))
//                .body("convictions[1].offences[0].description", equalTo("Arson - 05600"))
//                .body("convictions[1].offences[1].description", equalTo("Burglary (dwelling) with intent to commit, or the commission of an offence triable only on indictment - 02801"))
//                .body("convictions[1].sentence.description", equalTo("CJA - Indeterminate Public Prot."))
//                .body("convictions[1].sentence.length", equalTo(5))
//                .body("convictions[1].sentence.lengthUnits", equalTo("Years"))
//                .body("convictions[1].sentence.lengthInDays", equalTo(1826))
//                .body("convictions[1].sentence.convictionDate", equalTo(standardDateOf(2019, 9,3)))

//                .body("convictions[2].convictionId", equalTo("2500295343"))
//                .body("convictions[2].active", equalTo(true))
//                .body("convictions[2].offences[0].description", equalTo("Arson - 05600"))
//                .body("convictions[2].sentence.description", equalTo("CJA - Community Order"))
//                .body("convictions[2].sentence.length", equalTo(12))
//                .body("convictions[2].sentence.lengthUnits", equalTo("Months"))
//                .body("convictions[2].sentence.lengthInDays", equalTo(364))
//                .body("convictions[2].sentence.convictionDate", equalTo(standardDateOf(2019, 9,3)))
        ;

    }

    private String standardDateOf(int year, int month, int dayOfMonth) {
        return LocalDate.of(year, month, dayOfMonth).format(DateTimeFormatter.ISO_DATE);
    }
}
