package uk.gov.justice.probation.courtcaseservice.controller.model;

import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNotesRepository;
import uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = {"classpath:sql/before-common.sql", "classpath:case-progress.sql"}, config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CaseProgressIntTest extends BaseIntTest {

    private static final String HEARING_ID = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";
    private final String hearingNote = "{\n" +
        "        \"hearingId\": \"1f93aa0a-7e46-4885-a1cb-f25a4be33a00\",\n" +
        "        \"note\": \"Judge heard\",\n" +
        "        \"author\": \"Test Author\"\n" +
        "    }";

    @Autowired
    private HearingNotesRepository hearingNotesRepository;

    @Test
    void givenExistingCaseId_whenGetHearingByDefendantId_thenReturnCaseSummaryAlongWithAllHearings() {

        String testCaseId = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";

        String defendantId = "40db17d6-04db-11ec-b2d8-0242ac130002";
        String testHearingId = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";

        var response = given()
            .given()
            .auth()
            .oauth2(getToken())
            .when()
            .header("Accept", "application/json")
            .get("/hearing/{hearingId}/defendant/{defendantId}", testHearingId, defendantId)
            .then()
            .statusCode(200);

        response
            .body("caseId", Matchers.equalTo(testCaseId))
            .body("hearingId", Matchers.equalTo(testCaseId))
            .body("hearingType", Matchers.equalTo("Sentence"))
            .body("urn", Matchers.equalTo("URN008"))
            .body("offences", Matchers.hasSize(2))
            .body("probationStatus", Matchers.equalTo("Current"))
            .body("probationStatusActual", Matchers.equalTo("CURRENT"))
            .body("previouslyKnownTerminationDate", Matchers.equalTo(LocalDate.of(2010, Month.JANUARY, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .body("preSentenceActivity", Matchers.equalTo(true))
            .body("suspendedSentenceOrder", Matchers.equalTo(true))
            .body("breach", Matchers.equalTo(true))
            .body("source", Matchers.equalTo("COMMON_PLATFORM"))
            .body("crn", Matchers.equalTo("X320741"))
            .body("pnc", Matchers.equalTo("A/1234560BA"))
            .body("cro", Matchers.equalTo("311462/13E"))
            .body("defendantId", Matchers.equalTo(defendantId))
            .body("phoneNumber.mobile", Matchers.equalTo("07000000007"))
            .body("phoneNumber.home", Matchers.equalTo("07000000013"))
            .body("phoneNumber.work", Matchers.equalTo("07000000015"))
            .body("name.title", Matchers.equalTo("Mr"))
            .body("name.forename1", Matchers.equalTo("Johnny"))
            .body("name.forename2", Matchers.equalTo("John"))
            .body("name.forename3", Matchers.equalTo("Jon"))
            .body("name.surname", Matchers.equalTo("BALL"))

            .body("hearings", Matchers.hasSize(2))
            .body("hearings[0].hearingId", Matchers.equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00"))
            .body("hearings[0].court", Matchers.equalTo("North Shields"))
            .body("hearings[0].courtRoom", Matchers.equalTo("2"))
            .body("hearings[0].session", Matchers.equalTo("MORNING"))
            .body("hearings[0].hearingTypeLabel", Matchers.equalTo("Sentence"))
            .body("hearings[0].hearingDateTime", Matchers.equalTo("2019-11-14T09:00:00"))

            .body("hearings[0].notes", Matchers.hasSize(2))
            .body("hearings[0].notes[0].noteId", Matchers.equalTo(-1700028800))
            .body("hearings[0].notes[0].hearingId", Matchers.equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00"))
            .body("hearings[0].notes[0].note", Matchers.equalTo("Judge heard"))
            .body("hearings[0].notes[0].created", Matchers.notNullValue())
            .body("hearings[0].notes[0].author", Matchers.equalTo("Author One"))
            .body("hearings[0].notes[0].createdByUuid", Matchers.equalTo("fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81"))
            .body("hearings[0].notes[1].noteId", Matchers.equalTo(-1700028802))
            .body("hearings[0].notes[1].hearingId", Matchers.equalTo("1f93aa0a-7e46-4885-a1cb-f25a4be33a00"))
            .body("hearings[0].notes[1].note", Matchers.equalTo("Judge sentenced"))
            .body("hearings[0].notes[1].created", Matchers.notNullValue())
            .body("hearings[0].notes[1].author", Matchers.equalTo("Author three"))
            .body("hearings[0].notes[1].createdByUuid", Matchers.equalTo("389fd9cf-390e-469a-b4cf-6c12024c4cae"))

            .body("hearings[1].hearingId", Matchers.equalTo("2aa6f5e0-f842-4939-bc6a-01346abc09e7"))
            .body("hearings[1].court", Matchers.equalTo("Leicester"))
            .body("hearings[1].courtRoom", Matchers.equalTo("2"))
            .body("hearings[1].session", Matchers.equalTo("MORNING"))
            .body("hearings[1].hearingTypeLabel", Matchers.equalTo("Hearing"))
            .body("hearings[1].hearingDateTime", Matchers.equalTo("2019-10-14T09:00:00"))

            .body("hearings[1].notes", Matchers.hasSize(1))
            .body("hearings[1].notes[0].noteId", Matchers.equalTo(-1700028803))
            .body("hearings[1].notes[0].hearingId", Matchers.equalTo("2aa6f5e0-f842-4939-bc6a-01346abc09e7"))
            .body("hearings[1].notes[0].note", Matchers.equalTo("Judge requested PSR"))
            .body("hearings[1].notes[0].created", Matchers.notNullValue())
            .body("hearings[1].notes[0].author", Matchers.equalTo("Author Three"))
            .body("hearings[1].notes[0].createdByUuid", Matchers.equalTo("fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81"))
        ;
    }

    @Test
    void givenExistingHearingId_whenCreateHearingNote_shouldCreateSuccessfully() {

        Response hearingNoteResponse = given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(hearingNote)
            .when()
            .post("/hearing/{hearingId}/notes", HEARING_ID);
        hearingNoteResponse
            .then()
            .statusCode(201)
            .body("hearingId", Matchers.equalTo(HEARING_ID))
            .body("note", Matchers.equalTo("Judge heard"))
            .body("author", Matchers.equalTo("Test Author"))
            .body("createdByUuid", Matchers.equalTo(TokenHelper.TEST_UUID))
            .body("created", Matchers.notNullValue())
        ;
        var hearingNote = hearingNoteResponse.getBody().as(HearingNoteResponse.class, ObjectMapperType.JACKSON_2);

        var hearingNoteEntity = hearingNotesRepository.findById(hearingNote.getNoteId()).get();
        assertThat(hearingNoteEntity.getNote()).isEqualTo(hearingNote.getNote());
        assertThat(hearingNoteEntity.getCreatedByUuid()).isEqualTo(hearingNote.getCreatedByUuid());
        assertThat(hearingNoteEntity.getAuthor()).isEqualTo(hearingNote.getAuthor());
        assertThat(hearingNoteEntity.isDeleted()).isFalse();

        Assertions.assertNotNull(hearingNoteEntity);
    }
}
