package uk.gov.justice.probation.courtcaseservice.controller;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PhoneNumberEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseInitService;
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType;

import java.nio.file.Files;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = "classpath:sql/before-common.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:sql/hearing-outcomes.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
class CourtCaseControllerPutHearingAfterOutcomeIntTest extends BaseIntTest {

    @Autowired
    CourtCaseInitService courtCaseInitService;

    ObjectMapper objectMapper;

    private static final LocalDateTime sessionStartTime = LocalDateTime.of(2019, 12, 14, 9, 0);
    private static final String PUT_BY_HEARING_ID_ENDPOINT = "/hearing/{hearingId}";

    private static final String CASE_ID = "1f93aa0a-7e46-4885-a1cb-f25a4be33a00";
    private static final String HEARING_ID = "2aa6f5e0-f842-4939-bc6a-01346abc09e7";
    private static final String URN = "URN007";

    @Value("classpath:integration/request/PUT_courtCaseExtendedHearing_success.json")
    private Resource caseDetailsExtendedResource;

    private String caseDetailsExtendedJson;

    @BeforeEach
    void beforeEach() throws Exception {
        objectMapper = new ObjectMapper();
        caseDetailsExtendedJson = Files.readString(caseDetailsExtendedResource.getFile().toPath());
        getEmittedEventsQueueSqsClient().purgeQueue(
            PurgeQueueRequest.builder()
                .queueUrl(getEmittedEventsQueueUrl())
                .build());
    }

    @Test
    void whenHearingWithOutcomeExists_thenPutHearingUpdatesSuccessfully() {
        given()
                .auth()
                .oauth2(getToken())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(caseDetailsExtendedJson)
                .when()
                .put(PUT_BY_HEARING_ID_ENDPOINT, HEARING_ID)
                .then()
                .statusCode(201);

        var cc = courtCaseInitService.initializeHearing(HEARING_ID);
        cc.ifPresentOrElse(hearingEntity -> {
            assertThat(hearingEntity.getCaseId()).isEqualTo(CASE_ID);
            assertThat(hearingEntity.getHearingId()).isEqualTo(HEARING_ID);
            assertThat(hearingEntity.getListNo()).isEqualTo("4");
            assertThat(hearingEntity.getCourtCase().getUrn()).isEqualTo(URN);
            assertThat(hearingEntity.getHearingEventType().getName()).isEqualTo("ConfirmedOrUpdated");
            assertThat(hearingEntity.getHearingType()).isEqualTo("sentenced");
            assertThat(hearingEntity.getHearingDefendants().getFirst().getOffences()).extracting("listNo").containsOnly(5, 8);
            assertThat(hearingEntity.getHearingDefendants().getFirst().getDefendant().getPhoneNumber()).isEqualTo(
                    PhoneNumberEntity.builder().home("07000000013").mobile("07000000014").work("07000000015").build());
            assertThat(hearingEntity.getHearingDefendants().getFirst().getDefendant().getPersonId()).isNotBlank();
            assertThat(hearingEntity.getHearingDefendants().getFirst().getHearingOutcome().getOutcomeType()).isEqualTo(HearingOutcomeType.ADJOURNED.name());
        }, () -> fail("Court case not updated as expected"));

    }

}
