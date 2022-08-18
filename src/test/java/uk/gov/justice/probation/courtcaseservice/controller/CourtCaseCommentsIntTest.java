package uk.gov.justice.probation.courtcaseservice.controller;

import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseCommentResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CaseCommentsRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
class CourtCaseCommentsIntTest extends BaseIntTest {

    @Autowired
    CourtCaseRepository courtCaseRepository;

    @Autowired
    CaseCommentsRepository caseCommentsRepository;

    private static final String CASE_ID = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";
    private final String caseComment = "{\n" +
        "        \"caseId\": \"1f93aa0a-7e46-4885-a1cb-f25a4be33a00\",\n" +
        "        \"comment\": \"PSR is delayed\",\n" +
        "        \"author\": \"Test Author\"\n" +
        "    }";

    @Test
    void whenCreateCaseCommentByCaseId_shouldCreateSuccessfully() {

        Response caseCommentResponse = given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(caseComment)
            .when()
            .post("/cases/{caseId}/comments", CASE_ID);
        caseCommentResponse
            .then()
            .statusCode(201)
            .body("caseId", equalTo(CASE_ID))
            .body("comment", equalTo("PSR is delayed"))
            .body("author", equalTo("Test Author"))
            .body("createdByUuid", equalTo(TokenHelper.TEST_UUID))
            .body("created", notNullValue())
        ;
        var caseComment = caseCommentResponse.getBody().as(CaseCommentResponse.class, ObjectMapperType.JACKSON_2);

        var actualComment = caseCommentsRepository.findById(caseComment.getCommentId()).get();
        assertThat(actualComment.getId()).isEqualTo(caseComment.getCommentId());
        assertThat(actualComment.getCreatedByUuid()).isEqualTo(caseComment.getCreatedByUuid());
        assertThat(actualComment.isDeleted()).isFalse();

        Assertions.assertNotNull(actualComment);
    }

    @Test
    void givenCaseIdDoesNotExist_whenCreateCaseCommentByCaseId_shouldReturnNotFound() {

        final var notFoundCaseId = "47eec326-7ceb-4dc1-aa86-b30ce2ccad8c";
        Response caseCommentResponse = given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(caseComment.replace(CASE_ID, notFoundCaseId))
            .when()
            .post("/cases/{caseId}/comments", notFoundCaseId);
        caseCommentResponse
            .then()
            .statusCode(404)
            .body("userMessage", equalTo(String.format("Court case %s not found", notFoundCaseId)))
        ;
    }

    @Test
    void givenCaseIdAndCommentId_whenDeleteComment_shouldMarkCommentAsDeleted() {

        var commentId = -1700028902L;

        given()
            .auth()
            .oauth2(getToken("389fd9cf-390e-469a-b4cf-6c12024c4cae"))
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
            .delete("/cases/{caseId}/comments/{commentId}", CASE_ID, commentId)
            .then()
            .statusCode(200)
        ;

        var actualComment = caseCommentsRepository.findById(commentId).get();
        assertThat(actualComment.isDeleted()).isTrue();
    }

    @Test
    void givenNonExistingCommentId_whenDeleteComment_shouldThrowEntityNotFound() {

        var commentId = 123L;

        given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
            .delete("/cases/{caseId}/comments/{commentId}", CASE_ID, commentId)
            .then()
            .statusCode(404)
            .body("userMessage", equalTo(String.format("Comment %d not found", commentId)))
        ;
    }

    @Test
    void givenCaseIdCommentIdAndUserUuid_AndUserUuidDoesNotMatchCommentUserUuid_shouldReturnForbidden() {

        var commentId = -1700028902L;
        var invalidUUid = "4f7772a9-e42a-493a-a8f0-82caf83c6419";

        given()
            .auth()
            .oauth2(getToken(invalidUUid))
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
            .delete("/cases/{caseId}/comments/{commentId}", CASE_ID, commentId)
            .then()
            .statusCode(403)
            .body("userMessage", equalTo("User 4f7772a9-e42a-493a-a8f0-82caf83c6419 does not have permissions to delete comment -1700028902"))
        ;
    }
}
