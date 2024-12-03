package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingCourtCaseDTO;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
//
//    @MockBean
//    private TelemetryService telemetryService;

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

//        verify(telemetryService, times(2))
//                .trackDeleteHearingEvent(eq(TelemetryEventType.PIC_DELETE_HEARING), any(HearingCourtCaseDTO.class), eq(featureFlags.deleteHearing()));

        Optional<HearingEntity> hearingEntityNotDeleted = hearingRepositoryFacade.findById(-199L);
        assertThat(hearingEntityNotDeleted.isPresent()).isTrue();
        assertThat(hearingEntityNotDeleted.get().getId()).isEqualTo(-199L);
        assertThat(hearingEntityNotDeleted.get().isDeleted()).isFalse();
    }

    void testHearingSoftDeleted(HearingEntity hearing) {
        assertThat(hearing.isDeleted()).isTrue();
        assertThat(hearing.getCourtCase().isDeleted()).isTrue();
        assertThat(hearing.getHearingDefendants().size()).isEqualTo(0);
        assertThat(hearing.getHearingDays().size()).isEqualTo(0);
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
