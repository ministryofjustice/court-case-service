package uk.gov.justice.probation.courtcaseservice.service

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Bean
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenceMappaMappingRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenceSfoMappingRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepositoryCustom
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.service.flags.MultiAgencyPublicProtectionArrangementsFlagResolver
import uk.gov.justice.probation.courtcaseservice.service.flags.OffenceFlagHelper
import uk.gov.justice.probation.courtcaseservice.service.flags.SeriousFurtherOffenceFlagResolver
import java.time.LocalTime

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
  scripts = ["classpath:sql/before-common.sql", "classpath:sql/hearing-outcomes-unresulted-cases.sql"],
  config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
  executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
internal open class CaseWorkflowServiceIntTest {

  @Autowired
  lateinit var caseWorkflowService: CaseWorkflowService

  @Autowired
  lateinit var hearingRepository: HearingRepository

  @MockitoBean
  lateinit var telemetryService: TelemetryService

  @Test
  fun shouldMoveUnHeardCasesToOutcomesWorkflow() {
    // When running first time
    caseWorkflowService.processUnResultedCases()

    // Then
    val ho1 = hearingRepository.findFirstByHearingIdOrderByCreatedDesc("2aa6f5e0-f842-4939-bc6a-01346abc09e7").get().hearingDefendants[0].hearingOutcome

    assertThat(ho1.outcomeType).isEqualTo(HearingOutcomeType.ADJOURNED.name)
    assertThat(ho1.state).isEqualTo(HearingOutcomeItemState.RESULTED.name)

    val ho2 = hearingRepository.findFirstByHearingIdOrderByCreatedDesc("1f93aa0a-7e46-4885-a1cb-f25a4be33a00").get().hearingDefendants[0].hearingOutcome

    assertThat(ho2.outcomeType).isEqualTo(HearingOutcomeType.NO_OUTCOME.name)
    assertThat(ho2.state).isEqualTo(HearingOutcomeItemState.NEW.name)

    val ho3 = hearingRepository.findFirstByHearingIdOrderByCreatedDesc("ddfe6b75-c3fc-4ed0-9bf6-21d66b125636").get().hearingDefendants[0].hearingOutcome
    assertThat(ho3).isNull()
  }

  @Test
  fun shouldCreateHearingOutComeRecordOnlyOnce() {
    // When running first time
    caseWorkflowService.processUnResultedCases()

    // Then
    val ho1 = hearingRepository.findFirstByHearingIdOrderByCreatedDesc("2aa6f5e0-f842-4939-bc6a-01346abc09e7").get().hearingDefendants[0].hearingOutcome

    assertThat(ho1.outcomeType).isEqualTo(HearingOutcomeType.ADJOURNED.name)
    assertThat(ho1.state).isEqualTo(HearingOutcomeItemState.RESULTED.name)

    var hearingEntity = hearingRepository.findFirstByHearingIdOrderByCreatedDesc("1f93aa0a-7e46-4885-a1cb-f25a4be33a00").get()

    var hearingDefendantEntity = hearingEntity.hearingDefendants
    assertThat(hearingDefendantEntity.size).isEqualTo(1)

    var hearingOutcomeEntity = hearingDefendantEntity[0].hearingOutcome

    assertThat(hearingOutcomeEntity.outcomeType).isEqualTo(HearingOutcomeType.NO_OUTCOME.name)
    assertThat(hearingOutcomeEntity.state).isEqualTo(HearingOutcomeItemState.NEW.name)

    // run the job second time
    caseWorkflowService.processUnResultedCases()

    hearingEntity = hearingRepository.findFirstByHearingIdOrderByCreatedDesc("1f93aa0a-7e46-4885-a1cb-f25a4be33a00").get()

    hearingDefendantEntity = hearingEntity.hearingDefendants
    assertThat(hearingDefendantEntity.size).isEqualTo(1)

    hearingOutcomeEntity = hearingDefendantEntity[0].hearingOutcome

    assertThat(hearingOutcomeEntity.outcomeType).isEqualTo(HearingOutcomeType.NO_OUTCOME.name)
    assertThat(hearingOutcomeEntity.state).isEqualTo(HearingOutcomeItemState.NEW.name)
  }

  @org.springframework.boot.test.context.TestConfiguration
  open class TestConfiguration {
    @Bean
    open fun caseWorkflowService(
      @Autowired hearingRepository: HearingRepository,
      @Autowired hearingOutcomeRepositoryCustom: HearingOutcomeRepositoryCustom,
      @Autowired telemetryService: TelemetryService,
      @Autowired courtRepository: CourtRepository,
      @Autowired seriousFurtherOffenceFlagResolver: SeriousFurtherOffenceFlagResolver,
      @Autowired multiAgencyPublicProtectionArrangementsFlagResolver: MultiAgencyPublicProtectionArrangementsFlagResolver,
      @Value("\${hearing_outcomes.move_un_resulted_to_outcomes_cutoff_time}")
      @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
      cutOffTime: LocalTime,
    ): CaseWorkflowService {
      var hearingEntityInitService = HearingEntityInitService(hearingRepository)
      return CaseWorkflowService(
        hearingRepository,
        hearingEntityInitService,
        courtRepository,
        hearingOutcomeRepositoryCustom,
        telemetryService,
        seriousFurtherOffenceFlagResolver,
        multiAgencyPublicProtectionArrangementsFlagResolver,
        listOf(),
        cutOffTime,
      )
    }

    @Bean
    open fun pagedCaseListRepositoryCustom(entityManager: EntityManager): HearingOutcomeRepositoryCustom = HearingOutcomeRepositoryCustom(entityManager)

    @Bean
    open fun hearingOutcomeRepositoryCustom(entityManager: EntityManager): HearingOutcomeRepositoryCustom = HearingOutcomeRepositoryCustom(entityManager)

    @Bean
    open fun featureFlags(): FeatureFlags = FeatureFlags()

    @Bean
    open fun offenceFlagHelper(): OffenceFlagHelper = OffenceFlagHelper()

    @Bean
    open fun seriousFurtherOffenceFlagResolver(
      offenceSfoMappingRepository: OffenceSfoMappingRepository,
      featureFlags: FeatureFlags,
      offenceFlagHelper: OffenceFlagHelper,
    ): SeriousFurtherOffenceFlagResolver = SeriousFurtherOffenceFlagResolver(offenceSfoMappingRepository, featureFlags, offenceFlagHelper)

    @Bean
    open fun multiAgencyPublicProtectionArrangementsFlagResolver(
      offenceMappaMappingRepository: OffenceMappaMappingRepository,
      featureFlags: FeatureFlags,
      offenceFlagHelper: OffenceFlagHelper,
    ): MultiAgencyPublicProtectionArrangementsFlagResolver =
      MultiAgencyPublicProtectionArrangementsFlagResolver(offenceMappaMappingRepository, featureFlags, offenceFlagHelper)
  }
}
