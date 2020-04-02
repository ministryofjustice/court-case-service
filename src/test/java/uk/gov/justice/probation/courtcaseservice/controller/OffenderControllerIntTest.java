package uk.gov.justice.probation.courtcaseservice.controller;


import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.TestConfig;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = "test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "org.apache.catalina.connector.RECYCLE_FACADES=true")
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
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/offender/NOT-THERE/probation-record")
                .then()
                .statusCode(404)
                    .body("userMessage", equalTo("Offender with CRN 'NOT-THERE' not found"))
                    .body("developerMessage" , equalTo("Offender with CRN 'NOT-THERE' not found"));
    }

    @Test
    public void whenCallMadeToGetOffenderData_thenReturnCorrectData() {
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
                .get("/offender/X320741/probation-record")
        .then()
                .statusCode(200)
                .body("crn",  equalTo("X320741"))
                .body("offenderManagers[0].forenames", equalTo("Temperance"))
                .body("offenderManagers[0].surname", equalTo("Brennan"))
                .body("offenderManagers[0].allocatedDate", equalTo(standardDateOf(2019, 9, 30)))

                .body("convictions[0].convictionId", equalTo("2500297061"))
                .body("convictions[0].active", equalTo(false))
                .body("convictions[0].offences[0].description", equalTo("Assault on Police Officer - 10400"))
                .body("convictions[0].sentence.description", equalTo("Absolute/Conditional Discharge"))
                .body("convictions[0].sentence.length", equalTo(0))
                .body("convictions[0].sentence.lengthUnits", equalTo("Months"))
                .body("convictions[0].sentence.lengthInDays", equalTo(0))
                .body("convictions[0].convictionDate", equalTo(standardDateOf(2019, 9,16)))

                .body("convictions[1].convictionId", equalTo("2500295345"))
                .body("convictions[1].active", equalTo(true))
                .body("convictions[1].offences[0].description", equalTo("Arson - 05600"))
                .body("convictions[1].offences[1].description", equalTo("Burglary (dwelling) with intent to commit, or the commission of an offence triable only on indictment - 02801"))
                .body("convictions[1].sentence.description", equalTo("CJA - Indeterminate Public Prot."))
                .body("convictions[1].sentence.length", equalTo(5))
                .body("convictions[1].sentence.lengthUnits", equalTo("Years"))
                .body("convictions[1].sentence.lengthInDays", equalTo(1826))
                .body("convictions[1].convictionDate", equalTo(standardDateOf(2019, 9,3)))

                .body("convictions[2].convictionId", equalTo("2500295343"))
                .body("convictions[2].active", equalTo(false))
                .body("convictions[2].offences[0].description", equalTo("Arson - 05600"))
                .body("convictions[2].sentence.description", equalTo("CJA - Community Order"))
                .body("convictions[2].sentence.length", equalTo(12))
                .body("convictions[2].sentence.lengthUnits", equalTo("Months"))
                .body("convictions[2].sentence.lengthInDays", equalTo(364))
                .body("convictions[2].convictionDate", equalTo(standardDateOf(2017, 6,1)))
        ;

    }

    @Test
    public void whenCallMadeToGetRequirementData_thenReturnCorrectData() {
          given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                  .when()
                      .get("/offender/X320741/convictions/2500297061/requirements")
                            .then()
                            .statusCode(200)
                            .body("requirements[0].rqmntTypeMainCategoryId",  equalTo("11"))
                            .body("requirements[0].rqmntTypeSubCategoryId", equalTo("1256"))
                            .body("requirements[0].adRqmntTypeMainCategoryId", equalTo(null))
                            .body("requirements[0].adRqmntTypeSubCategoryId", equalTo(null))
                            .body("requirements[0].length", equalTo(60))
                            .body("requirements[0].startDate", equalTo(standardDateOf(2017, 6,1)))
                            .body("requirements[0].terminationDate", equalTo(standardDateOf(2017, 12, 1)))
                            .body("requirements[0].rqmntTerminationReasonId", equalTo("2500052883"))

                            .body("requirements[1].rqmntTypeMainCategoryId",  equalTo("12345677"))
                            .body("requirements[1].rqmntTypeSubCategoryId", equalTo("1256"))
                            .body("requirements[1].adRqmntTypeMainCategoryId", equalTo(null))
                            .body("requirements[1].adRqmntTypeSubCategoryId", equalTo(null))
                            .body("requirements[1].length", equalTo(60))
                            .body("requirements[1].startDate", equalTo(standardDateOf(2019, 6,1)))
                            .body("requirements[1].terminationDate", equalTo(standardDateOf(2019, 12,1)))
                            .body("requirements[1].rqmntTerminationReasonId", equalTo("2500052885"))


                            .body("requirements[2].rqmntTypeMainCategoryId",  equalTo("1778990"))
                            .body("requirements[2].rqmntTypeSubCategoryId", equalTo("1256789"))
                            .body("requirements[2].adRqmntTypeMainCategoryId", equalTo(null))
                            .body("requirements[2].adRqmntTypeSubCategoryId", equalTo(null))
                            .body("requirements[2].length", equalTo(60))
                            .body("requirements[2].startDate", equalTo(standardDateOf(2018, 6,1)))
                            .body("requirements[2].terminationDate", equalTo(standardDateOf(2018, 12,1)))
                            .body("requirements[2].rqmntTerminationReasonId", equalTo("2500052884"))

        ;

    }

    private String standardDateOf(int year, int month, int dayOfMonth) {
        return LocalDate.of(year, month, dayOfMonth).format(DateTimeFormatter.ISO_DATE);
    }
}
