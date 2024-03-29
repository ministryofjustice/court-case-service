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
    private final String caseComment = """
            {
                    "caseId": "1f93aa0a-7e46-4885-a1cb-f25a4be33a00",
                    "comment": "PSR is delayed",
                    "author": "Test Author"
                }""";

    @Test
    void whenCreateCaseCommentByCaseId_shouldCreateSuccessfully() {

        Response caseCommentResponse = given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(caseComment)
            .when()
            .post("/cases/{caseId}/defendants/{defendantId}/comments", CASE_ID, "40db17d6-04db-11ec-b2d8-0242ac130002");
        caseCommentResponse
            .then()
            .statusCode(201)
            .body("caseId", equalTo(CASE_ID))
            .body("comment", equalTo("PSR is delayed"))
            .body("author", equalTo("Test Author"))
            .body("createdByUuid", equalTo(TokenHelper.TEST_UUID))
            .body("created", notNullValue())
            .body("draft", equalTo(false))
        ;
        var caseComment = caseCommentResponse.getBody().as(CaseCommentResponse.class, ObjectMapperType.JACKSON_2);

        var actualComment = caseCommentsRepository.findById(caseComment.getCommentId()).get();
        assertThat(actualComment.getId()).isEqualTo(caseComment.getCommentId());
        assertThat(actualComment.getCreatedByUuid()).isEqualTo(caseComment.getCreatedByUuid());
        assertThat(actualComment.isDeleted()).isFalse();
        assertThat(actualComment.isDraft()).isFalse();

        Assertions.assertNotNull(actualComment);
    }
    @Test
    void whenCreateUpdateCaseCommentByCaseId_shouldCreateSuccessfully() {

        Response caseCommentResponse = given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(caseComment)
            .when()
            .put("/cases/{caseId}/defendants/{defendantId}/comments/draft", CASE_ID, "40db17d6-04db-11ec-b2d8-0242ac130002");
        caseCommentResponse
            .then()
            .statusCode(200)
            .body("caseId", equalTo(CASE_ID))
            .body("comment", equalTo("PSR is delayed"))
            .body("author", equalTo("Test Author"))
            .body("createdByUuid", equalTo(TokenHelper.TEST_UUID))
            .body("draft", equalTo(true))
            .body("created", notNullValue())
        ;
        var caseComment = caseCommentResponse.getBody().as(CaseCommentResponse.class, ObjectMapperType.JACKSON_2);

        var actualComment = caseCommentsRepository.findById(caseComment.getCommentId()).get();
        assertThat(actualComment.getId()).isEqualTo(caseComment.getCommentId());
        assertThat(actualComment.getCreatedByUuid()).isEqualTo(caseComment.getCreatedByUuid());
        assertThat(actualComment.isDeleted()).isFalse();
        assertThat(actualComment.isDraft()).isTrue();

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
            .post("/cases/{caseId}/defendants/{defendantId}/comments", notFoundCaseId, "1f93aa0a-7e46-4885-a1cb-f25a4be33a00");
        caseCommentResponse
            .then()
            .statusCode(404)
            .body("userMessage", equalTo("Court case 47eec326-7ceb-4dc1-aa86-b30ce2ccad8c / defendantId 1f93aa0a-7e46-4885-a1cb-f25a4be33a00 not found"))
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
            .delete("/cases/{caseId}/defendants/{defendantId}/comments/{commentId}", CASE_ID, "40db17d6-04db-11ec-b2d8-0242ac130002", commentId)
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
            .delete("/cases/{caseId}/defendants/{defendantId}/comments/{commentId}", CASE_ID, "40db17d6-04db-11ec-b2d8-0242ac130002", commentId)
            .then()
            .statusCode(404)
            .body("userMessage", equalTo("Comment 123 not found for caseId 1f93aa0a-7e46-4885-a1cb-f25a4be33a00, defendantId 40db17d6-04db-11ec-b2d8-0242ac130002 and user fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81 or user does not have permissions to modify"))
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
            .delete("/cases/{caseId}/defendants/{defendantId}/comments/{commentId}", CASE_ID, "40db17d6-04db-11ec-b2d8-0242ac130002", commentId)
            .then()
            .statusCode(404)
            .body("userMessage", equalTo("Comment -1700028902 not found for caseId 1f93aa0a-7e46-4885-a1cb-f25a4be33a00, defendantId 40db17d6-04db-11ec-b2d8-0242ac130002 and user 4f7772a9-e42a-493a-a8f0-82caf83c6419 or user does not have permissions to modify"))
        ;
    }

    @Test
    void givenCaseIdAndCommentId_whenDeleteCommentDraft_shouldDeleteCaseCommentDraft() {

        given()
            .auth()
            .oauth2(getToken("389fd9cf-390e-469a-b4cf-6c12024c4cae"))
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
            .delete("/cases/{caseId}/defendants/{defendantId}/comments/draft", CASE_ID, "40db17d6-04db-11ec-b2d8-0242ac130002")
            .then()
            .statusCode(200)
        ;

        assertThat(caseCommentsRepository.findById(-1700028903L).isPresent()).isFalse();
    }

    @Test
    void givenExistingCaseAndComment_whenUpdateCaseComment_shouldUpdateSuccessfully() {

        var commentId = -1700028901L;
        var commentUpdate = "PSR completed with updated comment";
        Response caseCommentResponse = given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(caseComment.replace("PSR is delayed", commentUpdate))
            .when()
            .put("/cases/{caseId}/defendants/{defendantId}/comments/{commentId}", CASE_ID, "20db17d6-04db-11ec-b2d8-0242ac130002", commentId);
        caseCommentResponse
            .then()
            .statusCode(200)
            .body("caseId", equalTo(CASE_ID))
            .body("comment", equalTo(commentUpdate))
            .body("author", equalTo("Author One"))
            .body("createdByUuid", equalTo(TokenHelper.TEST_UUID))
            .body("draft", equalTo(false))
            .body("created", notNullValue())
        ;
        var caseComment = caseCommentResponse.getBody().as(CaseCommentResponse.class, ObjectMapperType.JACKSON_2);

        var actualComment = caseCommentsRepository.findById(commentId).get();
        assertThat(actualComment.getId()).isEqualTo(caseComment.getCommentId());
        assertThat(actualComment.getComment()).isEqualTo(commentUpdate);
    }
}
