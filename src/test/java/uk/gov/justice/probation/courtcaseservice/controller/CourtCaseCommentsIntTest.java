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
        "        \"userUuid\": \"805843a0-caf4-45f7-9711-98b69ba01f83\",\n" +
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
            .body("created", notNullValue())
        ;
        var caseComment = caseCommentResponse.getBody().as(CaseCommentResponse.class, ObjectMapperType.JACKSON_2);

        var actualComment = caseCommentsRepository.findById(caseComment.getCommentId()).get();
        assertThat(actualComment.getId()).isEqualTo(caseComment.getCommentId());
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
}
