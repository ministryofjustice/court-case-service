package uk.gov.justice.probation.courtcaseservice.service;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@Sql(scripts = { "classpath:sql/before-common.sql",
        "classpath:sql/before-HearingRepositoryDeleteIntTest.sql" }, config = @SqlConfig(transactionMode = ISOLATED), executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class DeleteHearingsServiceIntTest extends BaseIntTest {

    @Autowired
    private DeleteHearingsService deleteHearingsService;

    @Autowired
    private HearingRepositoryFacade hearingRepositoryFacade;

    @Autowired
    private FeatureFlags featureFlags;

    @Test
    void givenFeatureFlagEnabledAndDuplicateHearings_whenDeleteDuplicateHearing_ThenDuplicateHearingsAreDeleted() {
        featureFlags.setFlags(Map.of("delete-hearing", true));
        deleteHearingsService.deleteDuplicateHearings();
        Optional<HearingEntity> hearing1Deleted = hearingRepositoryFacade.findById(-198L);
        assertThat(hearing1Deleted.isPresent()).isTrue();
        testHearingSoftDeleted(hearing1Deleted.get());

        Optional<HearingEntity> hearing2Deleted = hearingRepositoryFacade.findById(-197L);
        assertThat(hearing2Deleted.isPresent()).isTrue();
        testHearingSoftDeleted(hearing2Deleted.get());

        Optional<HearingEntity> hearing3Deleted = hearingRepositoryFacade.findById(-298L);
        assertThat(hearing3Deleted.isPresent()).isTrue();
        testHearingSoftDeleted(hearing3Deleted.get());

        Optional<HearingEntity> hearingEntityNotDeleted = hearingRepositoryFacade.findById(-199L);
        assertThat(hearingEntityNotDeleted.isPresent()).isTrue();
        testHearingNotSoftDeleted(hearingEntityNotDeleted.get());
    }

    void testHearingNotSoftDeleted(HearingEntity hearing) {
        assertThat(hearing.isDeleted()).isFalse();
        assertThat(hearing.getCourtCase().isDeleted()).isFalse();
        hearing.getCourtCase().getCaseMarkers().forEach(caseMarker -> assertThat(caseMarker.isDeleted()).isFalse());
        hearing.getCourtCase().getCaseDefendants().forEach(caseDefendant -> {
            assertThat(caseDefendant.isDeleted()).isFalse();
            caseDefendant.getDocuments().forEach(document -> assertThat(document.isDeleted()).isFalse());
        });
        hearing.getHearingDefendants().forEach(hearingDefendant -> {
            assertThat(hearingDefendant.isDeleted()).isFalse();
            hearingDefendant.getOffences().forEach(offence -> assertThat(offence.isDeleted()).isFalse());
            assertThat(hearingDefendant.getHearingOutcome().isDeleted()).isFalse();
        });
        hearing.getHearingDays().forEach(hearingDay -> assertThat(hearingDay.isDeleted()).isFalse());
    }

    void testHearingSoftDeleted(HearingEntity hearing) {
        assertThat(hearing.isDeleted()).isTrue();
        assertThat(hearing.getCourtCase().isDeleted()).isTrue();
        hearing.getCourtCase().getCaseMarkers().forEach(caseMarker -> assertThat(caseMarker.isDeleted()).isTrue());
        hearing.getCourtCase().getCaseDefendants().forEach(caseDefendant -> {
            assertThat(caseDefendant.isDeleted()).isTrue();
            caseDefendant.getDocuments().forEach(document -> assertThat(document.isDeleted()).isTrue());
        });
        hearing.getHearingDefendants().forEach(hearingDefendant -> {
            assertThat(hearingDefendant.isDeleted()).isTrue();
            hearingDefendant.getOffences().forEach(offence -> assertThat(offence.isDeleted()).isTrue());
            if(hearingDefendant.getHearingOutcome() != null) {
                assertThat(hearingDefendant.getHearingOutcome().isDeleted()).isTrue();
            }
        });
        hearing.getHearingDays().forEach(hearingDay -> assertThat(hearingDay.isDeleted()).isTrue());
    }

    @Test
    void givenFeatureFlagDisabled_whenDeleteDuplicateHearings_thenNoHearingsDeleted() {
        deleteHearingsService.deleteDuplicateHearings();
        Optional<HearingEntity> hearing1Deleted = hearingRepositoryFacade.findById(-198L);
        assertThat(hearing1Deleted.isPresent()).isTrue();
        assertThat(hearing1Deleted.get().isDeleted()).isFalse();

        Optional<HearingEntity> hearing2Deleted = hearingRepositoryFacade.findById(-197L);
        assertThat(hearing2Deleted.isPresent()).isTrue();
        assertThat(hearing2Deleted.get().isDeleted()).isFalse();

        Optional<HearingEntity> hearingEntityNotDeleted = hearingRepositoryFacade.findById(-199L);
        assertThat(hearingEntityNotDeleted.isPresent()).isTrue();
        assertThat(hearingEntityNotDeleted.get().getId()).isEqualTo(-199L);
        assertThat(hearingEntityNotDeleted.get().isDeleted()).isFalse();
    }
}
