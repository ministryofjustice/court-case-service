package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

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

    @Test
    void setDeleteHearingsService() {
        deleteHearingsService.deleteDuplicateHearings();
        Optional<HearingEntity> hearingEntityDeleted = hearingRepositoryFacade.findById(-198L);
        assertThat(hearingEntityDeleted.isPresent()).isTrue();
        assertThat(hearingEntityDeleted.get().isDeleted()).isTrue();
        assertThat(hearingEntityDeleted.get().getCourtCase().isDeleted()).isTrue();

        Optional<HearingEntity> hearingEntityNotDeleted = hearingRepositoryFacade.findById(-199L);
        assertThat(hearingEntityNotDeleted.get().getId()).isEqualTo(-199L);
        assertThat(hearingEntityNotDeleted.get().isDeleted()).isFalse();
    }
}
