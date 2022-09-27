package uk.gov.justice.probation.courtcaseservice.controller;

import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.microsoft.applicationinsights.boot.dependencies.apachecommons.io.FileUtils;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEventType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PhoneNumberEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.listener.EventMessage;
import uk.gov.justice.probation.courtcaseservice.service.model.event.DomainEventMessage;
import uk.gov.justice.probation.courtcaseservice.service.model.event.PersonReference;
import uk.gov.justice.probation.courtcaseservice.service.model.event.PersonReferenceType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static org.springframework.util.StreamUtils.copyToString;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.OFFENDER_PNC;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
class CourtCaseControllerPutByHearingIdIntTest extends BaseIntTest {

    @Autowired
    HearingRepositoryFacade courtCaseRepository;

    @Autowired
    OffenderRepository offenderRepository;

    ObjectMapper objectMapper;

    private static final String CRN = "X320741";
    private static final AddressPropertiesEntity ADDRESS = new AddressPropertiesEntity("27", "Elm Place", "Bangor", null, null, "ad21 5dr");
    private static final String NOT_FOUND_COURT_CODE = "LPL";
    private static final LocalDateTime sessionStartTime = LocalDateTime.of(2019, 12, 14, 9, 0);
    private static final String PUT_BY_HEARING_ID_ENDPOINT = "/hearing/{hearingId}";

    private static final String JSON_CASE_ID = "ac24a1be-939b-49a4-a524-21a3d228f8bc";
    private static final String JSON_HEARING_ID = "75e63d6c-5487-4244-a5bc-7cf8a38992db";
    private static final String URN = "URN007";
    @Value("classpath:integration/request/PUT_courtCaseExtended_update_invalid.json")
    private Resource invalidExtendedCaseResource;

    @Value("classpath:integration/request/PUT_courtCaseExtended_success.json")
    private Resource caseDetailsExtendedResource;

    @Value("classpath:integration/request/PUT_courtCaseExtended_invalidListNo.json")
    private Resource caseDetailsExtendedInvalidListNoResource;

    private final File caseDetailsExtendedUpdate = new File(getClass().getClassLoader().getResource("integration/request/PUT_courtCaseExtended_update_success.json").getFile());

    private String caseDetailsExtendedJson;

    @BeforeEach
    void beforeEach() throws Exception {
        objectMapper = new ObjectMapper();
        caseDetailsExtendedJson = Files.readString(caseDetailsExtendedResource.getFile().toPath());
        getEmittedEventsQueueSqsClient().purgeQueue(new PurgeQueueRequest(getEmittedEventsQueueUrl()));

    }

    @Test
    void whenCreateCaseByHearingId_thenCreateNewRecord() {

        given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetailsExtendedJson)
                .when()
                .put(PUT_BY_HEARING_ID_ENDPOINT, JSON_HEARING_ID)
                .then()
                .statusCode(201)
                .body("caseId", equalTo(JSON_CASE_ID))
                .body("hearingId", equalTo(JSON_HEARING_ID))
                .body("urn", equalTo(URN))
                .body("source", equalTo("COMMON_PLATFORM"))
                .body("hearingType", equalTo("sentenced"))
                .body("hearingEventType", equalTo("ConfirmedOrUpdated"))
                .body("defendants", hasSize(1))
                .body("defendants[0].offences", hasSize(2))
                .body("defendants[0].type", equalTo("PERSON"))
                .body("defendants[0].defendantId", equalTo("d1eefed2-04df-11ec-b2d8-0242ac130002"))
                .body("defendants[0].probationStatus", equalTo("PREVIOUSLY_KNOWN"))
                .body("defendants[0].sex", equalTo("M"))
                .body("defendants[0].name.forename1", equalTo("Dylan"))
                .body("defendants[0].phoneNumber.home", equalTo("07000000013"))
                .body("defendants[0].phoneNumber.mobile", equalTo("07000000014"))
                .body("defendants[0].phoneNumber.work", equalTo("07000000015"))
                .body("defendants[0].offences[0].judicialResults", hasSize(3))
                .body("defendants[0].offences[0].judicialResults[0].convictedResult", equalTo(false))
                .body("defendants[0].offences[0].judicialResults[0].label", equalTo("Label-1"))
                .body("defendants[0].offences[0].judicialResults[0].judicialResultTypeId", equalTo(null))
                .body("hearingDays", hasSize(1))
                .body("hearingDays[0].courtCode", equalTo("B14LO"))
                .body("hearingDays[0].courtRoom", equalTo("1"))
                .body("hearingDays[0].sessionStartTime", equalTo(sessionStartTime.format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("hearingDays", hasSize(1));

        var cc = courtCaseRepository.findFirstByHearingIdOrderByIdDesc(JSON_HEARING_ID);
        cc.ifPresentOrElse(hearingEntity -> {
            assertThat(hearingEntity.getCaseId()).isEqualTo(JSON_CASE_ID);
            assertThat(hearingEntity.getHearingId()).isEqualTo(JSON_HEARING_ID);
            assertThat(hearingEntity.getListNo()).isEqualTo("4");
            assertThat(hearingEntity.getCourtCase().getUrn()).isEqualTo(URN);
            assertThat(hearingEntity.getHearingEventType().getName()).isEqualTo("ConfirmedOrUpdated");
            assertThat(hearingEntity.getHearingType()).isEqualTo("sentenced");
            assertThat(hearingEntity.getHearingDefendants().get(0).getOffences()).extracting("listNo").containsOnly(5, 8);
            assertThat(hearingEntity.getHearingDefendants().get(0).getDefendant().getPhoneNumber()).isEqualTo(
                    PhoneNumberEntity.builder().home("07000000013").mobile("07000000014").work("07000000015").build());
        }, () -> fail("Court case not created as expected"));

        offenderRepository.findByCrn(CRN).ifPresentOrElse(off -> {
            assertThat(off.getCrn()).isEqualTo(CRN);
            assertThat(off.getProbationStatus()).isEqualTo(OffenderProbationStatus.PREVIOUSLY_KNOWN);
            assertThat(off.getAwaitingPsr()).isTrue();
            assertThat(off.isBreach()).isTrue();
            assertThat(off.isPreSentenceActivity()).isTrue();
            assertThat(off.isSuspendedSentenceOrder()).isTrue();
            assertThat(off.getPnc()).isEqualTo(OFFENDER_PNC);
            assertThat(off.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2018, Month.JUNE, 24));
        }, () -> fail("Offender values not updated as expected"));

    }

    @Disabled("This validation is rejecting cases in prod. Temporarily disabling whilst we determine if we can do validation on this field.")
    @Test
    void whenCreateCaseByHearingIdWithInvalidListNo_thenReturnBadRequest() throws IOException {

        given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(copyToString(caseDetailsExtendedInvalidListNoResource.getInputStream(), Charset.defaultCharset()))
                .when()
                .put(PUT_BY_HEARING_ID_ENDPOINT, JSON_HEARING_ID)
                .then()
                .statusCode(400)
                .body("userMessage", containsString("Only one of hearingDays[].listNo and defendants[].offences[].listNo must be provided"));
    }

    @Test
    void whenCreateCaseByHearingId_andNonNullableOffenderFieldsAreNull_thenCreateNewRecordWithDefaults() throws IOException {

        final var crn = "X723999";
        final var updatedJson = FileUtils.readFileToString(caseDetailsExtendedUpdate, "UTF-8")
                .replace("\"crn\": \"X320741\"", "\"crn\": \"" + crn + "\"")
                .replace("\"breach\": true,\n", "")
                .replace("\"preSentenceActivity\": true,\n", "")
                .replace("\"suspendedSentenceOrder\": true,\n", "");

        given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(PUT_BY_HEARING_ID_ENDPOINT, JSON_HEARING_ID)
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
    void givenExistingCase_whenCreateCaseByHearingIdUpdateCrn_thenCreateNewRecordUpdateCreateNewOffender() throws IOException {
        final var crn = "X721999";
        final var updatedJson = FileUtils.readFileToString(caseDetailsExtendedUpdate, "UTF-8")
                .replace("\"crn\": \"X320741\"", "\"crn\": \"" + crn + "\"");

        assertThat(offenderRepository.findByCrn(crn)).isNotPresent();

        given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(PUT_BY_HEARING_ID_ENDPOINT, JSON_HEARING_ID)
                .then()
                .statusCode(201)
                .body("caseId", equalTo("3db9d70b-10a2-49d1-b74d-379f2db74862"))
                .body("hearingId", equalTo(JSON_HEARING_ID))
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
    void givenUnknownCourt_whenCreateCourtCaseByHearingId_ThenOk() {
        var updatedJson = caseDetailsExtendedJson
                .replace("\"courtCode\": \"B14LO\"", "\"courtCode\": \"" + NOT_FOUND_COURT_CODE + "\"");

        given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(PUT_BY_HEARING_ID_ENDPOINT, JSON_HEARING_ID)
                .then()
                .statusCode(201);
    }

    @Test
    void givenMismatchHearingId_whenCreateCourtCase_ThenRaise400() {

        final var ALTERNATIVE_HEARING_ID = "6eb32e9f-ae7a-4d13-a026-b4a6f8e8731a";
        given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetailsExtendedJson)
                .when()
                .put(PUT_BY_HEARING_ID_ENDPOINT, ALTERNATIVE_HEARING_ID)
                .then()
                .statusCode(400)
                .body("developerMessage", equalTo("Hearing Id " + ALTERNATIVE_HEARING_ID + " does not match with value from body " + JSON_HEARING_ID))
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
                .put(PUT_BY_HEARING_ID_ENDPOINT, "1db2c76c-31a5-4b53-a46a-00681809515e")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
        ;
    }

    @Test
    void givenExistingCaseWithNoOffenderAttached_whenAddNewOffender_thenAddAndCreateOffender() {
        final var caseId = "ac24a1be-939b-49a4-a524-21a3d2230000";
        final var defendantId = "d49323c0-04da-11ec-b2d8-0242ac130002";
        final var newCrn = "X212786";
        final var updatedJson = caseDetailsExtendedJson
                .replace("\"caseId\": \"ac24a1be-939b-49a4-a524-21a3d228f8bc\"", "\"caseId\": \"" + caseId + "\"")
                .replace("\"defendantId\": \"d1eefed2-04df-11ec-b2d8-0242ac130002\"", "\"defendantId\": \"" + defendantId + "\"")
                .replace("\"crn\": \"X320741\"", "\"crn\": \"" + newCrn + "\"");

        assertThat(offenderRepository.findByCrn(newCrn)).isNotPresent();

        given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(PUT_BY_HEARING_ID_ENDPOINT, JSON_HEARING_ID)
                .then()
                .statusCode(201)
                .body("caseId", equalTo(caseId))
        ;

        // The correct offender is now associated
        courtCaseRepository.findFirstByHearingIdOrderByIdDesc(caseId)
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
    void givenExistingCaseWithNoOffenderAttached_whenAddExistingOffender_thenAdd() {
        final var caseId = "ac24a1be-939b-49a4-a524-21a3d2230000";
        final var defendantId = "d49323c0-04da-11ec-b2d8-0242ac130002";
        final var updatedJson = caseDetailsExtendedJson
                .replace("\"caseId\": \"ac24a1be-939b-49a4-a524-21a3d228f8bc\"", "\"caseId\": \"" + caseId + "\"")
                .replace("\"defendantId\": \"d1eefed2-04df-11ec-b2d8-0242ac130002\"", "\"defendantId\": \"" + defendantId + "\"");

        // No offenders associated with the defendants
        courtCaseRepository.findFirstByHearingIdOrderByIdDesc(caseId)
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
                .put(PUT_BY_HEARING_ID_ENDPOINT, JSON_HEARING_ID)
                .then()
                .statusCode(201)
                .body("caseId", equalTo(caseId))
                .body("hearingId", equalTo(JSON_HEARING_ID))
        ;

        // The correct offender is now associated
        courtCaseRepository.findFirstByHearingIdOrderByIdDesc(JSON_HEARING_ID)
                .ifPresentOrElse(theCase -> {
                    var defendants = theCase.getHearingDefendants();
                    assertThat(defendants).hasSize(1);
                    assertThat(defendants.get(0).getDefendant().getOffender().getCrn()).isEqualTo(CRN);
                }, () -> fail("Case should exist"));
    }

    @Test
    void givenExistingCaseWithOffenders_whenRemoveOffender_thenDetachOffenders() throws IOException {
        final var updatedJson = FileUtils.readFileToString(caseDetailsExtendedUpdate, "UTF-8")
                .replace("\"crn\": \"X320741\"", "\"crn\": null")
                .replace("\"crn\": \"X320742\"", "\"crn\": null");

        given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedJson)
                .when()
                .put(PUT_BY_HEARING_ID_ENDPOINT, JSON_HEARING_ID)
                .then()
                .statusCode(201)
                .body("caseId", equalTo("3db9d70b-10a2-49d1-b74d-379f2db74862"))
                .body("hearingId", equalTo(JSON_HEARING_ID))
        ;

        // No offenders associated with the defendants
        courtCaseRepository.findFirstByHearingIdOrderByIdDesc(JSON_HEARING_ID)
                .ifPresentOrElse(theCase -> {
                    assertThat(theCase.getHearingDefendants()
                            .stream()
                            .map(HearingDefendantEntity::getDefendant)
                            .filter(defendantEntity -> defendantEntity.getOffender() != null)
                            .toList()).isEmpty();
                }, () -> fail("Case should exist"));
    }

    @Test
    void givenExistingCaseWithConfirmedOrUpdateType_whenUpdateWithResultedHearingEventType_thenUpdateSuccessfully() throws IOException {

        final var crn = "X723999";
        var url = getEmittedEventsQueueUrl();

        final var createHearingJason = FileUtils.readFileToString(caseDetailsExtendedUpdate, "UTF-8");

        given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(createHearingJason)
                .when()
                .put(PUT_BY_HEARING_ID_ENDPOINT, JSON_HEARING_ID)
                .then()
                .statusCode(201)
                .body("caseId", equalTo("3db9d70b-10a2-49d1-b74d-379f2db74862"))
                .body("hearingId", equalTo(JSON_HEARING_ID))
                .body("hearingEventType", equalTo("ConfirmedOrUpdated"));
        ;

        courtCaseRepository.findFirstByHearingIdOrderByIdDesc(JSON_HEARING_ID)
                .ifPresentOrElse(theCase -> {
                    assertThat(theCase.getHearingEventType()).isEqualTo(HearingEventType.CONFIRMED_OR_UPDATED);

                }, () -> fail("Hearing event type should be ConfirmedOrUpdated"));

        assertThat(getEmittedEventsQueueSqsClient().receiveMessage(url).getMessages()).isEmpty();

        final var resultedHearingEventType = "Resulted";

        final var updatedHearingJson = FileUtils.readFileToString(caseDetailsExtendedUpdate, "UTF-8")
                .replace("\"hearingEventType\": \"ConfirmedOrUpdated\"", "\"hearingEventType\": \"" + resultedHearingEventType + "\"");

        given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(updatedHearingJson)
                .when()
                .put(PUT_BY_HEARING_ID_ENDPOINT, JSON_HEARING_ID)
                .then()
                .statusCode(201)
                .body("caseId", equalTo("3db9d70b-10a2-49d1-b74d-379f2db74862"))
                .body("hearingId", equalTo(JSON_HEARING_ID))
                .body("hearingEventType", equalTo(resultedHearingEventType));

        courtCaseRepository.findFirstByHearingIdOrderByIdDesc(JSON_HEARING_ID)
                .ifPresentOrElse(theCase -> {
                    assertThat(theCase.getHearingEventType()).isEqualTo(HearingEventType.RESULTED);

                }, () -> fail("Hearing event type should be Resulted"));

        await().atLeast(Duration.ofMillis(100));

        assertEmittedEventMessages();

    }

    private void assertEmittedEventMessages() throws IOException {
        // Must use this class when receiving more than one messages from a queue if not we always receive just 1
        var receiveMessageRequest = new ReceiveMessageRequest().withMaxNumberOfMessages(2).withQueueUrl(getEmittedEventsQueueUrl());

        var rawMessages = getEmittedEventsQueueSqsClient().receiveMessage(receiveMessageRequest).getMessages();
        assertThat(rawMessages).isNotNull();
        assertThat(rawMessages).size().isEqualTo(2);

        var rawMessage1 = objectMapper.readValue(rawMessages.get(0).getBody(), EventMessage.class);
        assertThat(rawMessage1).isNotNull();

        var rawMessage2 = objectMapper.readValue(rawMessages.get(1).getBody(), EventMessage.class);
        assertThat(rawMessage2).isNotNull();

        var receivedSentencedDomainEventMessage1 = objectMapper.readValue(rawMessage1.getMessage(), DomainEventMessage.class);
        assertThat(receivedSentencedDomainEventMessage1).isNotNull();

        var receivedSentencedDomainEventMessage2 = objectMapper.readValue(rawMessage2.getMessage(), DomainEventMessage.class);
        assertThat(receivedSentencedDomainEventMessage2).isNotNull();


        var receivedDomainEventMessages = Arrays.asList(receivedSentencedDomainEventMessage1, receivedSentencedDomainEventMessage2);

        DomainEventMessage expectedDomainEventMessage1 = DomainEventMessage.builder()
                .eventType("court.case.sentenced")
                .detailUrl("https://localhost/hearing/75e63d6c-5487-4244-a5bc-7cf8a38992db")
                .version(1)
                .personReference(PersonReference.builder()
                        .identifiers(buildDefendantIdentifiers("X320741", "99999", "A/1234560BA"))
                        .build())
                .build();
        DomainEventMessage expectedDomainEventMessage2 = DomainEventMessage.builder()
                .eventType("court.case.sentenced")
                .detailUrl("https://localhost/hearing/75e63d6c-5487-4244-a5bc-7cf8a38992db")
                .version(1)
                .personReference(PersonReference.builder()
                        .identifiers(buildDefendantIdentifiers("X320742", "100000", "A/1234560BB"))
                        .build())
                .build();

        assertThat(receivedDomainEventMessages)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("occurredAt")
                .containsExactlyInAnyOrder(expectedDomainEventMessage1, expectedDomainEventMessage2);
    }

    private List<PersonReferenceType> buildDefendantIdentifiers(String crn, String cro, String pnc) {
        return List.of(
                PersonReferenceType.builder().type("CRN").value(crn).build(),
                PersonReferenceType.builder().type("CRO").value(cro).build(),
                PersonReferenceType.builder().type("PNC").value(pnc).build()
        );
    }
}
