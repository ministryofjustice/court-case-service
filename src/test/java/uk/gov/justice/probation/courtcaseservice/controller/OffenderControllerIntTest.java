package uk.gov.justice.probation.courtcaseservice.controller;


import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.RetryService;
import uk.gov.justice.probation.courtcaseservice.TestConfig;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static uk.gov.justice.probation.courtcaseservice.TestConfig.WIREMOCK_PORT;

@RunWith(SpringRunner.class)
@EnableRetry
@ActiveProfiles(profiles = "test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "org.apache.catalina.connector.RECYCLE_FACADES=true")
public class OffenderControllerIntTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RetryService retryService;

    @Before
    public void setUp() throws Exception {
        TestConfig.configureRestAssuredForIntTest(port);

        retryService.tryWireMockStub();
    }

    @ClassRule
    public static  final WireMockClassRule wireMockRule = new WireMockClassRule(wireMockConfig()
                                                                .port(WIREMOCK_PORT)
                                                                .usingFilesUnderClasspath("mocks"));

    @Rule
    public WireMockClassRule instanceRule = wireMockRule;

    @Test
    public void givenOffenderDoesNotExist_whenCallMadeToGetProbationRecord_thenReturnNotFound() {
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/offender/CRNXXX/probation-record")
                .then()
                .statusCode(404)
                    .body("userMessage", equalTo("Offender with CRN 'CRNXXX' not found"))
                    .body("developerMessage" , equalTo("Offender with CRN 'CRNXXX' not found"));
    }

    @Test
    public void whenCallMadeToGetProbationRecord_thenReturnCorrectData() {
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
                .body("convictions[0].inBreach", equalTo(true))
                .body("convictions[0].offences[0].description", equalTo("Assault on Police Officer - 10400"))
                .body("convictions[0].sentence.description", equalTo("Absolute/Conditional Discharge"))
                .body("convictions[0].sentence.length", equalTo(0))
                .body("convictions[0].sentence.lengthUnits", equalTo("Months"))
                .body("convictions[0].sentence.lengthInDays", equalTo(0))
                .body("convictions[0].sentence.terminationDate", equalTo(standardDateOf(2020, 1, 1)))
                .body("convictions[0].sentence.terminationReason", equalTo("Auto Terminated"))
                .body("convictions[0].sentence", not(hasKey("unpaidWork")))
                .body("convictions[0].convictionDate", equalTo(standardDateOf(2019, 9,16)))
                .body("convictions[0].documents", hasSize(0))

                .body("convictions[1].convictionId", equalTo("2500295345"))
                .body("convictions[1].active", equalTo(true))
                .body("convictions[1].inBreach", equalTo(false))
                .body("convictions[1].offences[0].description", equalTo("Arson - 05600"))
                .body("convictions[1].offences[1].description", equalTo("Burglary (dwelling) with intent to commit, or the commission of an offence triable only on indictment - 02801"))
                .body("convictions[1].sentence.description", equalTo("CJA - Indeterminate Public Prot."))
                .body("convictions[1].sentence.length", equalTo(5))
                .body("convictions[1].sentence.lengthUnits", equalTo("Years"))
                .body("convictions[1].sentence.lengthInDays", equalTo(1826))
                .body("convictions[1].sentence.terminationDate", equalTo(standardDateOf(2019, 1, 1)))
                .body("convictions[1].sentence.terminationReason", equalTo("ICMS Miscellaneous Event"))
                .body("convictions[1].convictionDate", equalTo(standardDateOf(2019, 9,3)))
                .body("convictions[1].documents", hasSize(1))
                .body("convictions[1].documents[0].documentId", equalTo("1d842fce-ec2d-45dc-ac9a-748d3076ca6b"))

                .body("convictions[2].convictionId", equalTo("2500295343"))
                .body("convictions[2].active", equalTo(null))
                .body("convictions[2].inBreach", equalTo(true))
                .body("convictions[2].offences[0].description", equalTo("Arson - 05600"))
                .body("convictions[2].convictionDate", equalTo(null))
                .body("convictions[2].documents", hasSize(0))

        ;
    }

    @Test
    public void whenCallMadeToGetProbationRecordNotFiltered_thenReturnExtraDocuments() {
        given()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
                .get("/offender/X320741/probation-record?applyDocTypeFilter=false")
            .then()
                .statusCode(200)
                .body("crn",  equalTo("X320741"))
                .body("convictions[0].convictionId", equalTo("2500297061"))
                .body("convictions[0].documents", hasSize(0))
                .body("convictions[1].convictionId", equalTo("2500295345"))
                .body("convictions[1].documents", hasSize(9))
                .body("convictions[2].convictionId", equalTo("2500295343"))
                .body("convictions[2].documents", hasSize(6))
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
                    .body("requirements[0].requirementId", equalTo(2500083652L))
                    .body("requirements[0].startDate", equalTo(standardDateOf(2017, 6,1)))
                    .body("requirements[0].terminationDate", equalTo(standardDateOf(2017, 12, 1)))
                    .body("requirements[0].expectedStartDate", equalTo(standardDateOf(2017, 6,1)))
                    .body("requirements[0].expectedEndDate", equalTo(standardDateOf(2017, 12, 1)))
                    .body("requirements[0].active", is(false))
                    .body("requirements[0].requirementTypeSubCategory.code",  equalTo("W01"))
                    .body("requirements[0].requirementTypeSubCategory.description", equalTo("Regular"))
                    .body("requirements[0].requirementTypeMainCategory.code",  equalTo("W"))
                    .body("requirements[0].requirementTypeMainCategory.description", equalTo("Unpaid Work"))
                    .body("requirements[0].terminationReason.code",  equalTo("74"))
                    .body("requirements[0].terminationReason.description", equalTo("Hours Completed Outside 12 months (UPW only)"))
                    .body("requirements[0].length", equalTo(60))
                    .body("requirements[0].lengthUnit", equalTo("Hours"))
                    .body("requirements[1].requirementId",  equalTo(2500007925L))
                    .body("requirements[1].startDate", equalTo(standardDateOf(2015, 7,1)))
                    .body("requirements[1].commencementDate", equalTo(standardDateOf(2015, 6,29)))
                    .body("requirements[1].active", is(true))
                    .body("requirements[1].adRequirementTypeMainCategory.code",  equalTo("7"))
                    .body("requirements[1].adRequirementTypeMainCategory.description", equalTo("Court - Accredited Programme"))
                    .body("requirements[1].adRequirementTypeSubCategory.code",  equalTo("P12"))
                    .body("requirements[1].adRequirementTypeSubCategory.description", equalTo("ASRO"))
        ;

    }

    @Test
    public void whenCallMadeToGetBreach_thenReturnCorrectData() {
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/offender/D003080/convictions/2500005095/breaches/2500003903")
                .then()
                .statusCode(200)
                .body("breachId", equalTo(2500003903L))
                .body("incidentDate", equalTo("2017-03-21"))
                .body("started", equalTo("2017-03-22"))
                .body("provider", equalTo("CPA West Yorkshire"))
                .body("team", equalTo("Unallocated"))
                .body("officer", equalTo("Unallocated Staff"))
                .body("status", equalTo("Induction Completed - Opted Out"))
        ;
    }

    @Test
    public void whenBreachDoesNotExist_thenReturn404() {
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/offender/D003080/convictions/2500005095/breach/1230000000")
                .then()
                .statusCode(404)
        ;
    }

    private String standardDateOf(int year, int month, int dayOfMonth) {
        return LocalDate.of(year, month, dayOfMonth).format(DateTimeFormatter.ISO_DATE);
    }

}
