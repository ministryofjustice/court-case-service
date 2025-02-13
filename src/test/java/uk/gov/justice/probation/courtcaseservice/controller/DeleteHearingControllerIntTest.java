package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade;

import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.testUtil.TokenHelper.getToken;

@Sql(scripts = {"classpath:sql/before-common.sql",
        "classpath:sql/before-HearingRepositoryDeleteIntTest.sql" }, config = @SqlConfig(transactionMode = ISOLATED), executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
class DeleteHearingControllerIntTest extends BaseIntTest {

    @Autowired
    private HearingRepositoryFacade hearingRepositoryFacade;

    @Autowired
    private FeatureFlags featureFlags;

    @Test
    void when_delete_hearing_enabled_deleteHearingSuccessfully() {
        featureFlags.setFlags(Map.of("delete-hearing", true));
        givenDeleteHearing();
        expectedHearingsSetToDeleted();
        expectedHearingsNotSetToDeleted();
    }

    @Test
    void when_delete_hearing_called_more_than_once_deleteHearingSuccessfully() {
        featureFlags.setFlags(Map.of("delete-hearing", true));
        givenDeleteHearing();
        givenDeleteHearing();
        givenDeleteHearing();
        expectedHearingsSetToDeleted();
        expectedHearingsNotSetToDeleted();
    }

    @Test
    void when_delete_hearing_disabled_deleteHearingSuccessfully() {
        featureFlags.setFlags(Map.of("delete-hearing", false));
        givenDeleteHearing();
        noHearingsSetToDeleted();
    }

    private static void givenDeleteHearing() {
        given()
                .auth()
                .oauth2(getToken())
                .when()
                .delete("/hearing/delete-duplicates")
                .then()
                .assertThat()
                .statusCode(200);
    }

    private void expectedHearingsSetToDeleted() {
        Optional<HearingEntity> firstHearingEntityDeleted = hearingRepositoryFacade.findById(-198L);
        assertTrue(firstHearingEntityDeleted.isPresent());
        assertThat(firstHearingEntityDeleted.get().isDeleted()).isTrue();

        Optional<HearingEntity> secondHearingEntityDeleted = hearingRepositoryFacade.findById(-197L);
        assertTrue(secondHearingEntityDeleted.isPresent());
        assertThat(secondHearingEntityDeleted.get().isDeleted()).isTrue();

        Optional<HearingEntity> thirdHearingEntityDeleted = hearingRepositoryFacade.findById(-298L);
        assertTrue(thirdHearingEntityDeleted.isPresent());
        assertThat(secondHearingEntityDeleted.get().isDeleted()).isTrue();
    }

    private void expectedHearingsNotSetToDeleted() {
        Optional<HearingEntity> hearingEntity = hearingRepositoryFacade.findById(-199L);
        assertTrue(hearingEntity.isPresent());
        assertThat(hearingEntity.get().isDeleted()).isFalse();

        Optional<HearingEntity> hearingEntity2 = hearingRepositoryFacade.findById(-299L);
        assertTrue(hearingEntity2.isPresent());
        assertThat(hearingEntity.get().isDeleted()).isFalse();

        Optional<HearingEntity> hearingEntity3 = hearingRepositoryFacade.findById(-200L);
        assertTrue(hearingEntity3.isPresent());
        assertThat(hearingEntity.get().isDeleted()).isFalse();

    }

    private void noHearingsSetToDeleted() {
        Optional<HearingEntity> hearingEntity = hearingRepositoryFacade.findById(-199L);
        assertTrue(hearingEntity.isPresent());
        assertThat(hearingEntity.get().isDeleted()).isFalse();

        Optional<HearingEntity> firstHearingEntityDeleted = hearingRepositoryFacade.findById(-198L);
        assertTrue(firstHearingEntityDeleted.isPresent());
        assertThat(firstHearingEntityDeleted.get().isDeleted()).isFalse();

        Optional<HearingEntity> secondHearingEntityDeleted = hearingRepositoryFacade.findById(-197L);
        assertTrue(secondHearingEntityDeleted.isPresent());
        assertThat(secondHearingEntityDeleted.get().isDeleted()).isFalse();

        Optional<HearingEntity> thirdHearingEntityDeleted = hearingRepositoryFacade.findById(-298L);
        assertTrue(thirdHearingEntityDeleted.isPresent());
        assertThat(secondHearingEntityDeleted.get().isDeleted()).isFalse();
    }
}