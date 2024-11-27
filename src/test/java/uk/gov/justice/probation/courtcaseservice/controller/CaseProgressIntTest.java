package uk.gov.justice.probation.courtcaseservice.controller;

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
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNoteResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNotesRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.HearingEntityInitService;
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
    private static final String  DEFENDANT_ID = "40db17d6-04db-11ec-b2d8-0242ac130002";

    private final String hearingNote = """
            {
                    "hearingId": "1f93aa0a-7e46-4885-a1cb-f25a4be33a00",
                    "note": "Judge heard",
                    "author": "Test Author"
                }""";

    private final String noteUpdate = """
            {
                    "hearingId": "2aa6f5e0-f842-4939-bc6a-01346abc09e7",
                    "note": "Judge heard new update update",
                    "author": "Author Three"
                }""";

    @Autowired
    private HearingNotesRepository hearingNotesRepository;

    @Autowired
    private HearingEntityInitService hearingEntityInitService;

    @Autowired
    private HearingRepository hearingRepository;

    @Test
    void givenExistingCaseId_whenGetHearingByDefendantId_thenReturnCaseSummaryAlongWithAllHearings() {

        String testCaseId = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";
        String defendantId = "40db17d6-04db-11ec-b2d8-0242ac130002";
        String testHearingId = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";

        var response = given()
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

            .body("hearings", Matchers.hasSize(3))
            .body("hearings[0].court", Matchers.equalTo("North Shields"))
            .body("hearings[0].courtRoom", Matchers.equalTo("1"))
            .body("hearings[0].session", Matchers.equalTo("MORNING"))
            .body("hearings[0].hearingTypeLabel", Matchers.equalTo("Hearing"))
            .body("hearings[0].hearingDateTime", Matchers.equalTo("2019-12-14T09:00:00"))

            .body("hearings[1].hearingId", Matchers.equalTo("2aa6f5e0-f842-4939-bc6a-01346abc09e7"))
            .body("hearings[1].court", Matchers.equalTo("Leicester"))
            .body("hearings[1].courtRoom", Matchers.equalTo("2"))
            .body("hearings[1].session", Matchers.equalTo("MORNING"))
            .body("hearings[1].hearingTypeLabel", Matchers.equalTo("Hearing"))
            .body("hearings[1].hearingDateTime", Matchers.equalTo("2019-10-14T09:00:00"))

            .body("hearings[1].notes", Matchers.hasSize(2))
            .body("hearings[1].notes[0].noteId", Matchers.equalTo(-1700028804))
            .body("hearings[1].notes[0].hearingId", Matchers.equalTo("2aa6f5e0-f842-4939-bc6a-01346abc09e7"))
            .body("hearings[1].notes[0].note", Matchers.equalTo("Judge requested PSR"))
            .body("hearings[1].notes[0].created", Matchers.notNullValue())
            .body("hearings[1].notes[0].author", Matchers.equalTo("Author Three"))
            .body("hearings[1].notes[0].createdByUuid", Matchers.equalTo("fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81"))
            .body("hearings[1].notes[0].draft", Matchers.equalTo(true))
            .body("hearings[1].hearingOutcome.hearingOutcomeType", Matchers.equalTo("ADJOURNED"))
            .body("hearings[1].hearingOutcome.hearingOutcomeDescription", Matchers.equalTo("Adjourned"))
            .body("hearings[1].hearingOutcome.outcomeDate", Matchers.equalTo("2023-04-24T09:09:09"))

            .body("hearings[1].notes[1].noteId", Matchers.equalTo(-1700028803))
            .body("hearings[1].notes[1].hearingId", Matchers.equalTo("2aa6f5e0-f842-4939-bc6a-01346abc09e7"))
            .body("hearings[1].notes[1].note", Matchers.equalTo("Judge requested PSR"))
            .body("hearings[1].notes[1].created", Matchers.notNullValue())
            .body("hearings[1].notes[1].author", Matchers.equalTo("Author Three"))
            .body("hearings[1].notes[1].createdByUuid", Matchers.equalTo("fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81"))
            .body("hearings[1].notes[1].draft", Matchers.equalTo(false));
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
            .post("/hearing/{hearingId}/defendants/{defendantId}/notes", HEARING_ID, DEFENDANT_ID);
        hearingNoteResponse
            .then()
            .statusCode(201)
            .body("hearingId", Matchers.equalTo(HEARING_ID))
            .body("note", Matchers.equalTo("Judge heard"))
            .body("author", Matchers.equalTo("Test Author"))
            .body("createdByUuid", Matchers.equalTo(TokenHelper.TEST_UUID))
            .body("created", Matchers.notNullValue())
        ;

        var hearing = hearingEntityInitService.findFirstByHearingIdFullyInitialised(HEARING_ID).get();
        var hearingDefendant = hearing.getHearingDefendant(DEFENDANT_ID);

        var hearingNote = hearingNoteResponse.getBody().as(HearingNoteResponse.class, ObjectMapperType.JACKSON_2);

        var hearingNoteEntity = hearingDefendant.getNotes().stream().filter(it -> it.getId().equals(hearingNote.getNoteId())).findFirst().get();
        assertThat(hearingNoteEntity.getNote()).isEqualTo(hearingNote.getNote());
        assertThat(hearingNoteEntity.getCreatedByUuid()).isEqualTo(hearingNote.getCreatedByUuid());
        assertThat(hearingNoteEntity.getAuthor()).isEqualTo(hearingNote.getAuthor());
        assertThat(hearingNoteEntity.isDeleted()).isFalse();

        Assertions.assertNotNull(hearingNoteEntity);
    }

    @Test
    void givenExistingNoteId_whenUpdateHearingNote_shouldUpdateNoteSuccessfully() {

        final var noteId = -1700028802L;
        Response hearingNoteResponse = given()
            .auth()
            .oauth2(getToken("389fd9cf-390e-469a-b4cf-6c12024c4cae"))
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(hearingNote.replace("Judge heard", "Judge heard update"))
            .when()
            .put("/hearing/{hearingId}/defendants/{defendantId}/notes/{noteId}", HEARING_ID, DEFENDANT_ID, noteId);
        hearingNoteResponse
            .then()
            .statusCode(200);

        var hearingNoteEntity = hearingNotesRepository.findById(noteId).get();
        assertThat(hearingNoteEntity.getNote()).isEqualTo("Judge heard update");
        assertThat(hearingNoteEntity.isDeleted()).isFalse();
    }

    @Test
    void givenExistingHearingIdAndNoteId_whenDeleteHearingNote_shouldUpdateNoteAsDeleted() {

        var noteId = -1700028800L;
        Response hearingNoteResponse = given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(hearingNote)
            .when()
            .delete("/hearing/{hearingId}/defendants/{defendantId}/notes/{noteId}", HEARING_ID, "40db17d6-04db-11ec-b2d8-0242ac130002", noteId);
        hearingNoteResponse
            .then()
            .statusCode(200);

        var hearingNoteEntity = hearingNotesRepository.findById(noteId).get();
        assertThat(hearingNoteEntity.isDeleted()).isTrue();
    }


    @Test
    void givenExistingHearingId_draftDoNotExistAlready_whenPutDraftNote_shouldCreateNewDraftSuccessfully() {

        Response hearingNoteResponse = given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(hearingNote)
            .when()
            .put("/hearing/{hearingId}/defendants/{defendantId}/notes/draft", HEARING_ID, DEFENDANT_ID);
        hearingNoteResponse
            .then()
            .statusCode(200)
            .body("hearingId", Matchers.equalTo(HEARING_ID))
            .body("note", Matchers.equalTo("Judge heard"))
            .body("author", Matchers.equalTo("Test Author"))
            .body("createdByUuid", Matchers.equalTo(TokenHelper.TEST_UUID))
            .body("created", Matchers.notNullValue())
            .body("draft", Matchers.is(true));


        var hearing = hearingEntityInitService.findFirstByHearingIdFullyInitialised(HEARING_ID).get();
        var hearingDefendant = hearing.getHearingDefendant(DEFENDANT_ID);

        var hearingNote = hearingNoteResponse.getBody().as(HearingNoteResponse.class, ObjectMapperType.JACKSON_2);

        var hearingNoteEntity = hearingDefendant.getNotes().get(0);

        assertThat(hearingNoteEntity.getNote()).isEqualTo(hearingNote.getNote());
        assertThat(hearingNoteEntity.getCreatedByUuid()).isEqualTo(hearingNote.getCreatedByUuid());
        assertThat(hearingNoteEntity.getAuthor()).isEqualTo(hearingNote.getAuthor());
        assertThat(hearingNoteEntity.isDeleted()).isFalse();
        assertThat(hearingNoteEntity.isDraft()).isTrue();

        Assertions.assertNotNull(hearingNoteEntity);
    }

  @Test
    void givenExistingHearingId_draftDoNotExist_whenDeleteDraftNote_shouldDeleteDraftNoteSuccessfully() {

      var testHearingId = "2aa6f5e0-f842-4939-bc6a-01346abc09e7";
      Response hearingNoteResponse = given()
            .auth()
            .oauth2(getToken())
            .body(hearingNote)
            .when()
            .delete("/hearing/{hearingId}/defendants/{defendantId}/notes/draft", testHearingId, "40db17d6-04db-11ec-b2d8-0242ac130002");
        hearingNoteResponse
            .then()
            .statusCode(200);

        var hearingNoteEntity = hearingNotesRepository.findByHearingIdAndCreatedByUuidAndDraftIsTrue(testHearingId, "fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81").get();
        Assertions.assertNotNull(hearingNoteEntity);
        Assertions.assertTrue(hearingNoteEntity.isDeleted());
    }

    @Test
    void givenExistingHearingId_aDraftExistAlready_whenPutDraftNote_shouldUpdateDraftNote() {
        var hearingId = "2aa6f5e0-f842-4939-bc6a-01346abc09e7";
        var defendantId = "40db17d6-04db-11ec-b2d8-0242ac130002";

        Response hearingNoteResponse = given()
            .auth()
            .oauth2(getToken())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(noteUpdate)
            .when()
            .put("/hearing/{hearingId}/defendants/{defendantId}/notes/draft", hearingId, defendantId);
        hearingNoteResponse
            .then()
            .statusCode(200)
            .body("hearingId", Matchers.equalTo(hearingId))
            .body("note", Matchers.equalTo("Judge heard new update update"))
            .body("author", Matchers.equalTo("Author Three"))
            .body("createdByUuid", Matchers.equalTo(TokenHelper.TEST_UUID))
            .body("created", Matchers.notNullValue())
            .body("draft", Matchers.is(true));

        var hearingNote = hearingNoteResponse.getBody().as(HearingNoteResponse.class, ObjectMapperType.JACKSON_2);

        var hearing = hearingEntityInitService.findFirstByHearingIdFullyInitialised(hearingId).get();
        var hearingDefendant = hearing.getHearingDefendant(defendantId);

        var hearingNoteEntity = hearingDefendant.getNotes().stream().filter(HearingNoteEntity::isDraft).findAny().get();

        assertThat(hearingNoteEntity.getNote()).isEqualTo("Judge heard new update update");
        assertThat(hearingNoteEntity.getCreatedByUuid()).isEqualTo(hearingNote.getCreatedByUuid());
        assertThat(hearingNoteEntity.getAuthor()).isEqualTo("Author Three");
        assertThat(hearingNoteEntity.isDeleted()).isFalse();
        assertThat(hearingNoteEntity.isDraft()).isTrue();

        Assertions.assertNotNull(hearingNoteEntity);
    }

}
