package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.boot.dependencies.apachecommons.io.FileUtils;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PhoneNumberEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.util.StreamUtils.copyToString;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_PHONE_NUMBER_ENTITY;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_SEX;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.LIST_NO;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.NATIONALITY_1;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.NATIONALITY_2;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
class CourtCaseControllerPutIntTest extends BaseIntTest {

    /* before-test.sql sets up a court case in the database */

    @Autowired
    ObjectMapper mapper;

    @Autowired
    HearingRepository courtCaseRepository;

    @Autowired
    OffenderRepository offenderRepository;

    @Autowired
    GroupedOffenderMatchRepository matchRepository;

    private static final String PUT_BY_CASEID_AND_DEFENDANTID_PATH = "/case/%s/defendant/%s";
    private static final String PNC = "A/1234560BA";
    private static final String CRN = "X320741";
    private static final String COURT_ROOM = "1";
    private static final AddressPropertiesEntity ADDRESS = new AddressPropertiesEntity("27", "Elm Place", "Bangor", null, null, "ad21 5dr");
    private static final String NOT_FOUND_COURT_CODE = "LPL";
    private static final String DEFENDANT_NAME = "Mr Dylan Adam ARMSTRONG";
    private static final LocalDateTime sessionStartTime = LocalDateTime.of(2019, 12, 14, 9, 0);

    @Value("classpath:integration/request/PUT_courtCase_success.json")
    private Resource caseDetailsResource;

    private String caseDetailsJson;

    /** NEW values match those from JSON file incoming. */
    private static final String JSON_CASE_ID = "571b7172-4cef-435c-9048-d071a43b9dbf";
    private static final String JSON_DEFENDANT_ID = "e0056894-e8f8-42c2-ba9a-e41250c3d1a3";

    @BeforeEach
    void beforeEach() throws Exception {
        caseDetailsJson = Files.readString(caseDetailsResource.getFile().toPath());
    }

    @Nested
    class PutByCaseIdExtended {
        private static final String JSON_CASE_ID = "ac24a1be-939b-49a4-a524-21a3d228f8bc";
        private static final String JSON_HEARING_ID = "75e63d6c-5487-4244-a5bc-7cf8a38992db";

        @Value("classpath:integration/request/PUT_courtCaseExtended_update_invalid.json")
        private Resource invalidExtendedCaseResource;

        @Value("classpath:integration/request/PUT_courtCaseExtended_success.json")
        private Resource caseDetailsExtendedResource;

        @Value("classpath:integration/request/PUT_courtCaseExtended_invalidListNo.json")
        private Resource caseDetailsExtendedInvalidListNoResource;

        // Note: There's a bizarre stack overflow bug happening when using the same Resource pattern as above which is why
        // this file is being read in a different way. There's an urgent fix required so committing as is to fix later.
        // CF - tried this again after moving resources into the nested classes and exactly the same thing happens
        // TODO: tidy this up
        // BEGIN
        // @Value("classpath:integration/request/PUT_courtCaseExtended_update_success.json")
        // private Resource caseDetailsExtendedUpdateResource;
        private final File caseDetailsExtendedUpdate = new File(getClass().getClassLoader().getResource("integration/request/PUT_courtCaseExtended_update_success.json").getFile());
        // END

        private String caseDetailsExtendedJson;

        @BeforeEach
        void beforeEach() throws Exception {
            caseDetailsExtendedJson = Files.readString(caseDetailsExtendedResource.getFile().toPath());
        }

        @Test
        void whenCreateCaseExtendedByCaseId_thenCreateNewRecord() {

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetailsExtendedJson)
                .when()
                .put(String.format("/case/%s/extended", JSON_CASE_ID))
                .then()
                .statusCode(201)
                .body("caseId", equalTo(JSON_CASE_ID))
                .body("hearingId", equalTo(JSON_HEARING_ID))
                .body("source", equalTo("COMMON_PLATFORM"))
                .body("defendants", hasSize(1))
                .body("defendants[0].offences",  hasSize(2))
                .body("defendants[0].type",  equalTo("PERSON"))
                .body("defendants[0].defendantId",  equalTo("d1eefed2-04df-11ec-b2d8-0242ac130002"))
                .body("defendants[0].probationStatus", equalTo("PREVIOUSLY_KNOWN"))
                .body("defendants[0].sex", equalTo("M"))
                .body("defendants[0].name.forename1", equalTo("Dylan"))
                .body("defendants[0].phoneNumber.home", equalTo("07000000013"))
                .body("defendants[0].phoneNumber.mobile", equalTo("07000000014"))
                .body("defendants[0].phoneNumber.work", equalTo("07000000015"))
                .body("hearingDays", hasSize(1))
                .body("hearingDays[0].courtCode", equalTo("B14LO"))
                .body("hearingDays[0].courtRoom", equalTo("1"))
                .body("hearingDays[0].sessionStartTime", equalTo(sessionStartTime.format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("hearingDays", hasSize(1))
                .body("hearingDays", hasSize(1))
            ;

            var cc = courtCaseRepository.findByHearingIdAndDefendantId(JSON_CASE_ID, "d1eefed2-04df-11ec-b2d8-0242ac130002");
            cc.ifPresentOrElse(courtCaseEntity -> {
                assertThat(courtCaseEntity.getCaseId()).isEqualTo(JSON_CASE_ID);
                assertThat(courtCaseEntity.getHearingId()).isEqualTo(JSON_HEARING_ID);
                assertThat(courtCaseEntity.getHearingDefendants().get(0).getOffences()).extracting("listNo").containsOnly(5, 8);
                assertThat(courtCaseEntity.getHearingDefendants().get(0).getDefendant().getPhoneNumber()).isEqualTo(
                        PhoneNumberEntity.builder().home("07000000013").mobile("07000000014").work("07000000015").build());
            }, () -> fail("Court case not created as expected"));

            offenderRepository.findByCrn(CRN).ifPresentOrElse(off -> {
                assertThat(off.getCrn()).isEqualTo(CRN);
                assertThat(off.getProbationStatus()).isEqualTo(OffenderProbationStatus.PREVIOUSLY_KNOWN);
                assertThat(off.getAwaitingPsr()).isTrue();
                assertThat(off.isBreach()).isTrue();
                assertThat(off.isPreSentenceActivity()).isTrue();
                assertThat(off.isSuspendedSentenceOrder()).isTrue();
                assertThat(off.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2018, Month.JUNE, 24));
            }, () -> fail("Offender values not updated as expected"));

        }

        @Test
        void givenHearingIdIsAbsent_whenCreateCaseExtendedByCaseId_thenCreateNewRecordWithCaseIdAsHearingId() {

            given()
                    .auth()
                    .oauth2(getToken())
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body(caseDetailsExtendedJson.replace("  \"hearingId\": \"75e63d6c-5487-4244-a5bc-7cf8a38992db\",", ""))
                    .when()
                    .put(String.format("/case/%s/extended", JSON_CASE_ID))
                    .then()
                    .statusCode(201)
                    .body("caseId", equalTo(JSON_CASE_ID))
                    .body("hearingId", equalTo(JSON_CASE_ID))
            ;

            var cc = courtCaseRepository.findByHearingIdAndDefendantId(JSON_CASE_ID, "d1eefed2-04df-11ec-b2d8-0242ac130002");
            cc.ifPresentOrElse(courtCaseEntity -> {
                assertThat(courtCaseEntity.getCaseId()).isEqualTo(JSON_CASE_ID);
                assertThat(courtCaseEntity.getHearingId()).isEqualTo(JSON_CASE_ID);
            }, () -> fail("Court case not created as expected"));
        }

        @Disabled("This validation is rejecting cases in prod. Temporarily disabling whilst we determine if we can do validation on this field.")
        @Test
        void whenCreateCaseExtendedByCaseIdWithInvalidListNo_thenReturnBadRequest() throws IOException {

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(copyToString(caseDetailsExtendedInvalidListNoResource.getInputStream(), Charset.defaultCharset()))
                .when()
                .put(String.format("/case/%s/extended", JSON_CASE_ID))
                .then()
                .statusCode(400)
                .body("userMessage", containsString("Only one of hearingDays[].listNo and defendants[].offences[].listNo must be provided"));
        }

        @Test
        void whenCreateCaseExtendedByCaseId_andNonNullableOffenderFieldsAreNull_thenCreateNewRecordWithDefaults() throws IOException {

            final var crn = "X723999";
            final var updatedJson = FileUtils.readFileToString(caseDetailsExtendedUpdate, "UTF-8")
                    .replace("\"crn\": \"X320741\"", "\"crn\": \"" + crn + "\"")
                    .replace("\"breach\": true,\n", "")
                    .replace("\"preSentenceActivity\": true,\n", "")
                    .replace("\"suspendedSentenceOrder\": true,\n", "")
                    ;

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format("/case/%s/extended", "3db9d70b-10a2-49d1-b74d-379f2db74862"))
                .then()
                .statusCode(201)
            ;

            offenderRepository.findByCrn(crn).ifPresentOrElse(off -> {
                assertThat(off.isBreach()).isFalse();
                assertThat(off.isPreSentenceActivity()).isFalse();
                assertThat(off.isSuspendedSentenceOrder()).isFalse();
            }, () -> fail("Offender values not updated as expected"));
        }

        @Test
        void givenExistingCase_whenCreateCaseExtendedByCaseIdUpdateCrn_thenCreateNewRecordUpdateCreateNewOffender() throws IOException {
            final var crn = "X721999";
            final var updatedJson = FileUtils.readFileToString(caseDetailsExtendedUpdate, "UTF-8")
                .replace("\"crn\": \"X320741\"", "\"crn\": \"" + crn + "\"")
                ;

            assertThat(offenderRepository.findByCrn(crn)).isNotPresent();

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format("/case/%s/extended", "3db9d70b-10a2-49d1-b74d-379f2db74862"))
                .then()
                .statusCode(201)
                .body("caseId", equalTo("3db9d70b-10a2-49d1-b74d-379f2db74862"))
            ;

            offenderRepository.findByCrn(crn).ifPresentOrElse(off -> {
                assertThat(off.getProbationStatus()).isEqualTo(OffenderProbationStatus.PREVIOUSLY_KNOWN);
                assertThat(off.getAwaitingPsr()).isTrue();
                assertThat(off.isBreach()).isTrue();
                assertThat(off.isPreSentenceActivity()).isTrue();
                assertThat(off.isSuspendedSentenceOrder()).isTrue();
                assertThat(off.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2018, Month.JUNE, 24));
            }, () -> fail("Offender values not updated as expected for crn " + crn));
        }

        @Test
        void givenExistingCaseWithOffenders_whenRemoveOffender_thenDetachOffenders() throws IOException {
            final var updatedJson = FileUtils.readFileToString(caseDetailsExtendedUpdate, "UTF-8")
                .replace("\"crn\": \"X320741\"", "\"crn\": null")
                .replace("\"crn\": \"X320742\"", "\"crn\": null")
                ;

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format("/case/%s/extended", "3db9d70b-10a2-49d1-b74d-379f2db74862"))
                .then()
                .statusCode(201)
                .body("caseId", equalTo("3db9d70b-10a2-49d1-b74d-379f2db74862"))
            ;

            // No offenders associated with the defendants
            courtCaseRepository.findByCaseId("3db9d70b-10a2-49d1-b74d-379f2db74862")
                .ifPresentOrElse(theCase -> {
                        assertThat(theCase.getHearingDefendants()
                                            .stream()
                                            .map(HearingDefendantEntity::getDefendant)
                                            .filter(defendantEntity -> defendantEntity.getOffender() != null)
                                            .toList()).isEmpty();
                    }, () -> fail("Case should exist"));

        }

        @Test
        void givenExistingCaseWithNoOffenderAttached_whenAddExistingOffender_thenAdd() {
            final var caseId = "ac24a1be-939b-49a4-a524-21a3d2230000";
            final var defendantId = "d49323c0-04da-11ec-b2d8-0242ac130002";
            final var updatedJson = caseDetailsExtendedJson
                .replace("\"caseId\": \"ac24a1be-939b-49a4-a524-21a3d228f8bc\"", "\"caseId\": \"" + caseId + "\"")
                .replace("\"defendantId\": \"e0056894-e8f8-42c2-ba9a-e41250c3d1a3\"", "\"defendantId\": \"" + defendantId + "\"")
                ;

            // No offenders associated with the defendants
            courtCaseRepository.findByCaseId(caseId)
                .ifPresentOrElse(theCase -> {
                    var defendants = theCase.getHearingDefendants();
                    assertThat(defendants).hasSize(1);
                    assertThat(defendants.get(0).getDefendant().getOffender()).isNull();
                }, () -> fail("Case should exist"));

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format("/case/%s/extended", caseId))
                .then()
                .statusCode(201)
                .body("caseId", equalTo(caseId))
            ;

            // The correct offender is now associated
            courtCaseRepository.findByCaseId(caseId)
                .ifPresentOrElse(theCase -> {
                    var defendants = theCase.getHearingDefendants();
                    assertThat(defendants).hasSize(1);
                    assertThat(defendants.get(0).getDefendant().getOffender().getCrn()).isEqualTo(CRN);
                }, () -> fail("Case should exist"));
        }

        @Test
        void givenExistingCaseWithNoOffenderAttached_whenAddNewOffender_thenAddAndCreateOffender() {
            final var caseId = "ac24a1be-939b-49a4-a524-21a3d2230000";
            final var defendantId = "d49323c0-04da-11ec-b2d8-0242ac130002";
            final var newCrn = "X212786";
            final var updatedJson = caseDetailsExtendedJson
                .replace("\"caseId\": \"ac24a1be-939b-49a4-a524-21a3d228f8bc\"", "\"caseId\": \"" + caseId + "\"")
                .replace("\"defendantId\": \"e0056894-e8f8-42c2-ba9a-e41250c3d1a3\"", "\"defendantId\": \"" + defendantId + "\"")
                .replace("\"crn\": \"X320741\"", "\"crn\": \""+ newCrn + "\"")
                ;

            assertThat(offenderRepository.findByCrn(newCrn)).isNotPresent();

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format("/case/%s/extended", caseId))
                .then()
                .statusCode(201)
                .body("caseId", equalTo(caseId))
            ;

            // The correct offender is now associated
            courtCaseRepository.findByCaseId(caseId)
                .ifPresentOrElse(theCase -> {
                    var defendants = theCase.getHearingDefendants();
                    assertThat(defendants).hasSize(1);
                    assertThat(defendants.get(0).getDefendant().getOffender().getCrn()).isEqualTo(newCrn);
                }, () -> fail("Case should exist"));

            offenderRepository.findByCrn(newCrn).ifPresentOrElse(off -> {
                assertThat(off.getProbationStatus()).isEqualTo(OffenderProbationStatus.PREVIOUSLY_KNOWN);
                assertThat(off.getAwaitingPsr()).isTrue();
                assertThat(off.isBreach()).isTrue();
                assertThat(off.isPreSentenceActivity()).isTrue();
                assertThat(off.isSuspendedSentenceOrder()).isTrue();
                assertThat(off.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2018, Month.JUNE, 24));
            }, () -> fail("Offender values not updated as expected for crn " + newCrn));
        }

        @Test
        void givenUnknownCourt_whenCreateCourtCaseByCaseId_ThenOk() {
            var updatedJson = caseDetailsExtendedJson
                .replace("\"courtCode\": \"B14LO\"", "\"courtCode\": \"" + NOT_FOUND_COURT_CODE + "\"")
                ;

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format("/case/%s/extended", JSON_CASE_ID))
                .then()
                .statusCode(201);
        }

        @Test
        void givenMismatchCaseId_whenCreateCourtCase_ThenRaise400() {

            final var ALTERNATIVE_CASE_ID = "6eb32e9f-ae7a-4d13-a026-b4a6f8e8731a";
            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetailsExtendedJson)
                .when()
                .put(String.format("/case/%s/extended", ALTERNATIVE_CASE_ID))
                .then()
                .statusCode(400)
                .body("developerMessage", equalTo("Case Id " + ALTERNATIVE_CASE_ID + " does not match with value from body " + JSON_CASE_ID))
            ;
        }
        @Test
        void givenInvalidRequestBody_whenCreateCourtCase_ThenRaise400() throws IOException {

            final var invalidJson = Files.readString(invalidExtendedCaseResource.getFile().toPath());

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(invalidJson)
                .when()
                .put(String.format("/case/%s/extended", "1db2c76c-31a5-4b53-a46a-00681809515e"))
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
            ;
        }
    }

     @Nested
    class PutByCaseIdAndSingleDefendantId {

        private static final String LEICESTER_COURT_CODE = "B33HU";

        @Test
        void givenNewCaseWithSingleDefendantNoCrn_whenCreateCaseDataByCaseAndDefendantId_ThenCreateNewRecord() {

            final var caseId = "aca6e1f4-e9fe-4ac4-b38e-5322bf770fd0";
            final var defendantId = "c6df9428-8c81-4957-a67e-a0bfdd0351d3";
            var updatedJson = caseDetailsJson
                .replace("\"caseId\": \"571b7172-4cef-435c-9048-d071a43b9dbf\"", "\"caseId\": \"" + caseId + "\"")
                .replace("\"defendantId\": \"e0056894-e8f8-42c2-ba9a-e41250c3d1a3\"", "\"defendantId\": \"" + defendantId + "\"" +
                        ", \"phoneNumber\": {\"home\": \"07000000013\", \"mobile\": \"07000000015\", \"work\": \"07000000014\"}")
                .replace("\"crn\": \"X320741\"", "\"crn\": null");

            var validatableResponse = given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format(PUT_BY_CASEID_AND_DEFENDANTID_PATH, caseId, defendantId))
                .then()
                .statusCode(201);

            validateResponse(validatableResponse);

            // All parts of the save are not in the response - so we check extra
            courtCaseRepository.findByCaseId(caseId)
                .ifPresentOrElse(entity -> {
                    assertThat(entity.getHearingDays()).hasSize(1);
                    assertThat(entity.getHearingDefendants()).hasSize(1);
                    assertThat(entity.getHearingDefendants().get(0).getOffences()).hasSize(2);
                    assertThat(entity.getHearingDefendants().get(0).getDefendant().getOffender()).isNull();
                    assertThat(entity.getHearingDefendants().get(0).getDefendant().getPhoneNumber()).isEqualTo(DEFENDANT_PHONE_NUMBER_ENTITY);
                }, () -> fail("COURT CASE does not exist for " + caseId));
        }

        @Test
        void givenExistingCaseWithMultipleDefendants_whenUpdateCaseDataByCaseIdAndDefendantIdAddOffender_thenUpdate() {

            final var crn = "X320654";

            final var caseId = "3db9d70b-10a2-49d1-b74d-379f2db74862";
            final var defendantIdToUpdate = "1263de26-4a81-42d3-a798-bad802433318";
            final var defendantIdToRetain = "6f014c2e-8be3-4a12-a551-8377bd31a7b8";

            // Updated JSON will update the name
            var updatedJson = caseDetailsJson
                .replace("\"courtCode\": \"B10JQ\"", "\"courtCode\": \"" + LEICESTER_COURT_CODE + "\"")
                .replace("\"caseId\": \"571b7172-4cef-435c-9048-d071a43b9dbf\"", "\"caseId\": \"" + caseId + "\"")
                .replace("\"defendantId\": \"e0056894-e8f8-42c2-ba9a-e41250c3d1a3\"", "\"defendantId\": \"" + defendantIdToUpdate + "\"")
                .replace("\"crn\": \"X320741\"", "\"crn\": \"" + crn + "\"")
                ;

            // All parts of the save are not in the response - so we check extra
            courtCaseRepository.findByCaseId(caseId)
                .ifPresentOrElse(entity -> {
                    assertThat(entity.getHearingDefendants()).hasSize(2);
                    assertThat(entity.getHearingDefendants()).extracting("defendant.offender").containsOnlyNulls();
                }, () -> fail("COURT CASE does not exist for " + caseId));

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format(PUT_BY_CASEID_AND_DEFENDANTID_PATH, caseId, defendantIdToUpdate))
                .then()
                .statusCode(201)
                .body("source", equalTo("LIBRA"))
                .body("courtCode", equalTo(LEICESTER_COURT_CODE))
                .body("crn", equalTo(crn))
                .body("defendantId", equalTo(defendantIdToUpdate))
                .body("defendantSex", equalTo("M"))
                .body("offences", hasSize(2))
                .body("hearings", hasSize(2))
                .body("probationStatusActual", equalTo("PREVIOUSLY_KNOWN"))
            ;

            // All parts of the save are not in the response - so we check extra
            courtCaseRepository.findByCaseId(caseId)
                .ifPresentOrElse(entity -> {
                    assertThat(entity.getHearingDays()).hasSize(2);
                    assertThat(entity.getHearingDefendants()).hasSize(2);
                    assertThat(entity.getHearingDefendants()).extracting("defendantId").containsExactlyInAnyOrder(defendantIdToUpdate, defendantIdToRetain);
                    assertThat(entity.getHearingDefendants().stream()
                                        .filter(d -> d.getDefendantId().equalsIgnoreCase(defendantIdToUpdate))
                                        .map(HearingDefendantEntity::getDefendant)
                                        .map(d -> d.getOffender().getCrn())
                                        .findFirst().get())
                        .isEqualTo(crn);
                }, () -> fail("COURT CASE does not exist for " + caseId));
        }

        @Test
        void givenExistingCase_whenUpdateCaseDataByCaseIdAndDefendantId_thenUpdateCrnAndMatches() {

            final var crn = "X320654";
            final var caseNo = "1800028900";
            final var caseId = "3db9d70b-10a2-49d1-b74d-379f2db74862";
            final var defendantIdToUpdate = "1263de26-4a81-42d3-a798-bad802433318";

            // Updated JSON will update the CRN
            var updatedJson = caseDetailsJson
                .replace("\"caseNo\": \"1700028914\"", "\"caseNo\": \"" + caseNo + "\"")
                .replace("\"courtCode\": \"B10JQ\"", "\"courtCode\": \"" + LEICESTER_COURT_CODE + "\"")
                .replace("\"caseId\": \"571b7172-4cef-435c-9048-d071a43b9dbf\"", "\"caseId\": \"" + caseId + "\"")
                .replace("\"defendantId\": \"e0056894-e8f8-42c2-ba9a-e41250c3d1a3\"", "\"defendantId\": \"" + defendantIdToUpdate + "\"")
                .replace("\"crn\": \"X320741\"", "\"crn\": \"" + crn + "\"")
                ;

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format(PUT_BY_CASEID_AND_DEFENDANTID_PATH, caseId, defendantIdToUpdate))
                .then()
                .statusCode(201)
            ;

            // In this test we want to look at the matches
            var groupMatches = matchRepository.findByCaseIdAndDefendantId(caseId, defendantIdToUpdate);
            groupMatches.ifPresentOrElse((matches) -> {
                assertThat(matches.getOffenderMatches()).hasSize(2);
                var confirmedMatch = matches.getOffenderMatches().stream()
                    .filter(match -> crn.equals(match.getCrn()))
                    .findFirst()
                    .orElseThrow();
                assertThat(confirmedMatch.getConfirmed()).isTrue();
                assertThat(confirmedMatch.getRejected()).isFalse();

                var otherMatches = matches.getOffenderMatches().stream()
                    .filter(match -> !crn.equals(match.getCrn()))
                    .collect(Collectors.toList());
                assertThat(otherMatches).extracting("confirmed").containsOnly(Boolean.FALSE);
                assertThat(otherMatches).extracting("rejected").containsOnly(Boolean.TRUE);
            }, () -> fail("COURT CASE does not exist for e652eaae-1114-4593-8f56-659eb2baffcf"));

        }

        @Test
        void givenExistingCaseWithDefendantAndOffender_whenRemoveOffender_ThenRemoveButKeepOffenderRecord() {
            final var caseId = "683bcde4-611f-4487-9833-f68090507b74";
            final var defendantId = "f2c83643-8ebd-4609-9183-cd8c34984e33";
            var updatedJson = caseDetailsJson
                .replace("\"caseId\": \"571b7172-4cef-435c-9048-d071a43b9dbf\"", "\"caseId\": \"" + caseId + "\"")
                .replace("\"defendantId\": \"e0056894-e8f8-42c2-ba9a-e41250c3d1a3\"", "\"defendantId\": \"" + defendantId + "\"")
                .replace("\"crn\": \"X320741\"", "\"crn\": null")
                ;

            courtCaseRepository.findByHearingIdAndDefendantId(caseId, defendantId)
                    .ifPresentOrElse(courtCase -> {
                        var offender = courtCase.getHearingDefendants().stream()
                            .filter(defendantEntity -> defendantEntity.getDefendantId().equalsIgnoreCase(defendantId))
                            .findFirst()
                            .map(HearingDefendantEntity::getDefendant)
                            .map(DefendantEntity::getOffender)
                            .orElse(null);
                        assertThat(offender).isNotNull();
                    }, () -> fail("COURT CASE does not exist for " + caseId));

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format(PUT_BY_CASEID_AND_DEFENDANTID_PATH, caseId, defendantId))
                .then()
                .statusCode(201)
                .body("$", not(hasKey("crn")))
                .body("$", not(hasKey("awaitingPsr")))
                .body("$", not(hasKey("breach")))
                .body("$", not(hasKey("preSentenceActivity")))
                .body("$", not(hasKey("suspendedSentenceOrder")))
                .body("$", not(hasKey("previouslyKnownTerminationDate")))
                .body("probationStatus", equalTo("No record"))
            ;

            courtCaseRepository.findByHearingIdAndDefendantId(caseId, defendantId)
                .ifPresentOrElse(courtCase -> {
                    var defendant = courtCase.getHearingDefendants().stream()
                        .filter(defendantEntity -> defendantEntity.getDefendantId().equalsIgnoreCase(defendantId))
                        .findFirst()
                        .orElse(null);
                    assertThat(defendant.getDefendant().getOffender()).isNull();
                }, () -> fail("COURT CASE does not exist for " + caseId));
        }

        @Test
        void givenNewCaseWithMismatchedCaseId_whenCreateCourtCase_ThenRaise400() {
            final var caseId = "2dba205d-0562-4252-a6c7-f5d7e2dcd36f";

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetailsJson)
                .when()
                .put(String.format(PUT_BY_CASEID_AND_DEFENDANTID_PATH, caseId, "99999"))
                .then()
                .statusCode(400)
                .body("developerMessage", equalTo("Case Id " + caseId + " does not match with value from body " + JSON_CASE_ID))
            ;
        }

        @Test
        void givenNewCaseWithMismatchedDefendantId_whenCreateCourtCase_ThenRaise400() {

            final var defendantId = "2dba205d-0562-4252-a6c7-f5d7e2dcd36f";
            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetailsJson)
                .when()
                .put(String.format(PUT_BY_CASEID_AND_DEFENDANTID_PATH, JSON_CASE_ID, defendantId))
                .then()
                .statusCode(400)
                .body("developerMessage", equalTo("Defendant Id " + defendantId + " does not match the one in the CourtCaseEntity body as submitted " + JSON_DEFENDANT_ID))
            ;
        }

        @Test
        void givenUnknownCourtCodeInBody_whenUpdateCaseDataByCaseIdAndDefendantId_thenOk() {

            final var unknownCourt = "X10XX";
            final var caseId = "3db9d70b-10a2-49d1-b74d-379f2db74862";
            final var defendantIdToUpdate = "1263de26-4a81-42d3-a798-bad802433318";

            var updatedJson = caseDetailsJson
                .replace("\"caseNo\": \"1700028914\"", "\"caseNo\": \"1800028900\"")
                .replace("\"courtCode\": \"B10JQ\"", "\"courtCode\": \"" + unknownCourt + "\"")
                .replace("\"caseId\": \"571b7172-4cef-435c-9048-d071a43b9dbf\"", "\"caseId\": \"" + caseId + "\"")
                .replace("\"defendantId\": \"e0056894-e8f8-42c2-ba9a-e41250c3d1a3\"", "\"defendantId\": \"" + defendantIdToUpdate + "\"")
                ;

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format(PUT_BY_CASEID_AND_DEFENDANTID_PATH, caseId, defendantIdToUpdate))
                .then()
                .statusCode(201)
            ;
        }

        @Test
        void givenInvalidRequest_whenUpdateCaseDataByCaseIdAndDefendantId_thenRaise400() {

            final var caseId = "3db9d70b-10a2-49d1-b74d-379f2db74862";
            final var defendantIdToUpdate = "1263de26-4a81-42d3-a798-bad802433318";

            var updatedJson = caseDetailsJson
                .replace("\"courtCode\": \"" + COURT_CODE + "\"", "\"courtCode\": \"  \"")
                ;

            given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(String.format(PUT_BY_CASEID_AND_DEFENDANTID_PATH, caseId, defendantIdToUpdate))
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
            ;
        }

    }

    private void validateResponse(ValidatableResponse validatableResponse) {

        validatableResponse.body("caseNo", equalTo((String) null))
            .body("caseId", equalTo("aca6e1f4-e9fe-4ac4-b38e-5322bf770fd0"))
            .body("courtCode", equalTo(COURT_CODE))
            .body("courtRoom", equalTo(COURT_ROOM))
            .body("source", equalTo("LIBRA"))
            .body("probationStatus", equalTo("No record"))
            .body("sessionStartTime", equalTo(sessionStartTime.format(DateTimeFormatter.ISO_DATE_TIME)))
            .body("defendantType", equalTo("PERSON"))
            .body("defendantName", equalTo(DEFENDANT_NAME))
            .body("phoneNumber.home", equalTo("07000000013"))
            .body("phoneNumber.mobile", equalTo("07000000015"))
            .body("phoneNumber.work", equalTo("07000000014"))
            .body("defendantAddress.line1", equalTo(ADDRESS.getLine1()))
            .body("defendantAddress.line2", equalTo(ADDRESS.getLine2()))
            .body("defendantAddress.postcode", equalTo(ADDRESS.getPostcode()))
            .body("defendantAddress.line3", equalTo(ADDRESS.getLine3()))
            .body("defendantAddress.line4", equalTo(null))
            .body("defendantAddress.line5", equalTo(null))
            .body("offences", hasSize(2))
            .body("offences[0].offenceTitle", equalTo("Theft from a shop"))
            .body("offences[0].offenceSummary", equalTo("On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person."))
            .body("offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))
            .body("offences[0]", not(hasKey("courtCase")))
            .body("offences[1].offenceTitle", equalTo("Theft from a different shop"))
            .body("pnc", equalTo(PNC))
            .body("listNo", equalTo(LIST_NO))
            .body("defendantDob", equalTo(LocalDate.of(1958, 12, 14).format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .body("defendantSex", equalTo(DEFENDANT_SEX))
            .body("defendantId", notNullValue())
            .body("nationality1", equalTo(NATIONALITY_1))
            .body("nationality2", equalTo(NATIONALITY_2))
        ;
        validatableResponse
            .body("$", not(hasKey("crn")))
            .body("$", not(hasKey("awaitingPsr")))
            .body("$", not(hasKey("breach")))
            .body("$", not(hasKey("preSentenceActivity")))
            .body("$", not(hasKey("suspendedSentenceOrder")))
            .body("$", not(hasKey("previouslyKnownTerminationDate")))
            ;
    }
}
