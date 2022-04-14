package uk.gov.justice.probation.courtcaseservice.controller;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtCaseControllerOffenderUpdateIntTest extends BaseIntTest {

    private static final String DEFENDANT_ID = "40db17d6-04db-11ec-b2d8-0242ac130002";

    @Autowired
    DefendantRepository defendantRepository;

    @Autowired
    OffenderRepository offenderRepository;

    private static final LocalDate DECEMBER_14 = LocalDate.of(2019, Month.DECEMBER, 14);
    private static final LocalDate JAN_1_2010 = LocalDate.of(2010, 1, 1);
    private static final LocalDateTime DECEMBER_14_9AM = LocalDateTime.of(2019, Month.DECEMBER, 14, 9, 0);
    private static final String CASE_NO = "1600028913";
    private static final String NOT_FOUND_COURT_CODE = "LPL";

    @Test
    void GET_offender_givenDefendantId_whenGetOffender_thenReturnAssociatedOffender() {

        given()
            .auth()
            .oauth2(getToken())
            .when()
            .get("/defendant/{defendantId}/offender", DEFENDANT_ID)
            .then()
            .assertThat()
            .statusCode(200)
            .body("crn", equalTo("X320741"))
            .body("probationStatus", equalTo("CURRENT"))
            .body("previouslyKnownTerminationDate", equalTo("2010-01-01"))
            .body("awaitingPsr", equalTo(true))
            .body("breach", equalTo(true))
            .body("preSentenceActivity", equalTo(true))
            .body("suspendedSentenceOrder", equalTo(true))
        ;
    }

    @Test
    void GET_offender_givenDefendantIdWithNoCrn_whenGetOffender_thenReturnHttpNotFound() {

        given()
            .auth()
            .oauth2(getToken())
            .when()
            .get("/defendant/{defendantId}/offender", "03d0c6a4-b00f-499b-bbb6-1fa80b1d7cf4")
            .then()
            .assertThat()
            .statusCode(404)
            .body("userMessage", equalTo("Offender details not found for defendant 03d0c6a4-b00f-499b-bbb6-1fa80b1d7cf4"))
        ;
    }

    @Test
    void GET_offender_givenDefendantIdWithCrnButNoOffender_whenGetOffender_thenReturnHttpNotFound() {

        given()
            .auth()
            .oauth2(getToken())
            .when()
            .get("/defendant/{defendantId}/offender", "7420ce9b-8d56-4019-9e68-81a17f54327e")
            .then()
            .assertThat()
            .statusCode(404)
            .body("userMessage", equalTo("Offender details not found for defendant 7420ce9b-8d56-4019-9e68-81a17f54327e"))
        ;
    }

    @Test
    void DELETE_offender_givenDefendantId_whenDeleteOffender_thenRemoveAssociation() {

        String DEFENDANT_ID_FOR_DELETE = "9c2f11b0-1bca-4b24-85a1-315d67020b2c";
        given()
            .auth()
            .oauth2(getToken())
            .when()
            .delete("/defendant/{defendantId}/offender", DEFENDANT_ID_FOR_DELETE)
            .then()
            .assertThat()
            .statusCode(200);
        final var defendant = defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_FOR_DELETE).get();
        assertThat(defendant.getCrn()).isNullOrEmpty();
    }

    @Test
    void PUT_offender_givenDefendantIdAndUpdatedOffender_whenPutOffender_thenUpdateOffender() {

        final String offenderUpdate = "{\n" +
            "                \"crn\": \"Y320741\",\n" +
            "                \"probationStatus\": \"NOT_SENTENCED\",\n" +
            "                \"previouslyKnownTerminationDate\": \"2010-01-01\",\n" +
            "                \"awaitingPsr\": true,\n" +
            "                \"breach\": false,\n" +
            "                \"preSentenceActivity\": true,\n" +
            "                \"suspendedSentenceOrder\": true\n" +
            "            }";

        String DEFENDANT_ID_FOR_UPDATE = "d59762b6-2da7-4af0-a09f-7296d40f15ce";
        given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
            .body(offenderUpdate)
            .put("/defendant/{defendantId}/offender", DEFENDANT_ID_FOR_UPDATE)
            .then()
            .assertThat()
            .statusCode(200);

        final var offender = offenderRepository.findByCrn("Y320741").get();
        assertThat(offender.getProbationStatus()).isEqualTo(OffenderProbationStatus.NOT_SENTENCED);
        assertThat(offender.isBreach()).isFalse();
    }

    @Test
    void PUT_offender_givenDefendantIdWithNoOffenderAndGivenNewOffender_whenPutOffender_theCreateAndAssocaiteOffender() {

        final String offenderUpdate = "{\n" +
            "                \"crn\": \"Z320741\",\n" +
            "                \"probationStatus\": \"NOT_SENTENCED\",\n" +
            "                \"previouslyKnownTerminationDate\": \"2010-01-01\",\n" +
            "                \"awaitingPsr\": true,\n" +
            "                \"breach\": false,\n" +
            "                \"preSentenceActivity\": true,\n" +
            "                \"suspendedSentenceOrder\": true\n" +
            "            }";

        String DEFENDANT_ID_FOR_UPDATE_NEW_OFFENDER = "c34bfca0-1ff1-4dab-9db7-acd27392b31a";
        given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
            .body(offenderUpdate)
            .put("/defendant/{defendantId}/offender", DEFENDANT_ID_FOR_UPDATE_NEW_OFFENDER)
            .then()
            .assertThat()
            .statusCode(200);

        final var offender = offenderRepository.findByCrn("Z320741").get();
        assertThat(offender.getProbationStatus()).isEqualTo(OffenderProbationStatus.NOT_SENTENCED);
        assertThat(offender.isBreach()).isFalse();

        final var defendant = defendantRepository.findFirstByDefendantIdOrderByIdDesc(DEFENDANT_ID_FOR_UPDATE_NEW_OFFENDER).get();
        assertThat(defendant.getCrn()).isEqualTo("Z320741");
    }

}
