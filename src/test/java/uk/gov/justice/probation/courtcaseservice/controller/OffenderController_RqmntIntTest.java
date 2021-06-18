package uk.gov.justice.probation.courtcaseservice.controller;


import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.probation.courtcaseservice.testUtil.DateHelper.standardDateOf;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Deprecated(forRemoval = true, since = "requirements are now only needed via the conviction")
class OffenderController_RqmntIntTest extends BaseIntTest {

    private static final String KNOWN_CRN = "X320741";
    private static final String KNOWN_CONVICTION_ID = "2500297061";

    @Test
    void givenOnLicenceStatus_whenCallMadeToGetRequirementData_thenReturnWithNoPSS() {
        given()
            .auth()
            .oauth2(getToken())
                    .accept(MediaType.APPLICATION_JSON_VALUE)
       .when()
           .get(String.format("/offender/%s/convictions/%s/requirements", KNOWN_CRN, KNOWN_CONVICTION_ID))
       .then()
                    .statusCode(200)
                    .body("requirements.size()", is(2))
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
                    .body("pssRequirements.size()", is(0))
                    .body("licenceConditions.size()", is(2))
                    .body("licenceConditions[0].description", equalTo("Curfew Arrangement"))
                    .body("licenceConditions[0].subTypeDescription", equalTo("ETE - High intensity"))
                    .body("licenceConditions[0].startDate", equalTo(standardDateOf(2020, 2, 1)))
                    .body("licenceConditions[0].notes", equalTo("This is an example of licence condition notes"))
                    .body("licenceConditions[1].description", equalTo("Participate or co-op with Programme or Activities"))
        ;
    }

    @Test
    void givenPSSStatus_whenCallMadeToGetRequirementData_thenReturnWithPSS() {
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(String.format("/offender/%s/convictions/%s/requirements", "X320742", KNOWN_CONVICTION_ID))
            .then()
            .statusCode(200)
            .body("requirements.size()", is(2))
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
            .body("pssRequirements.size()", is(3))
            .body("pssRequirements[0].description", equalTo("Specified Activity"))
            .body("pssRequirements[0].subTypeDescription", equalTo("Adjourned - Pre-Sentence Report"))
            .body("pssRequirements[1].description", equalTo("Travel Restriction"))
            .body("pssRequirements[2].description", equalTo("UK Travel Restriction"))
            .body("licenceConditions.size()", is(0))
        ;
    }

    @Test
    void givenErrorFromServices_whenCallMadeToGetRequirementData_thenReturnEmptyLists() {
        given()
            .auth()
            .oauth2(getToken())
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get(String.format("/offender/%s/convictions/%s/requirements", KNOWN_CRN, "99999"))
            .then()
            .statusCode(200)
            .body("requirements.size()", is(0))
            .body("pssRequirements.size()", is(0))
            .body("licenceConditions.size()", is(0))
            ;
    }

}
