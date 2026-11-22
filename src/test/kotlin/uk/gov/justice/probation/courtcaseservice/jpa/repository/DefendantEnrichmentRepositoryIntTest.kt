package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedDefendantDetails
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedGroupedOffenderMatches
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedMatchIdentifiers
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedName
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedOffenderDetails
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedOffenderMatch
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedProbationDetails
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentResultRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentSourceType
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichmentStatus
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import uk.gov.justice.probation.courtcaseservice.service.EnrichmentResultService
import uk.gov.justice.probation.courtcaseservice.service.OffenderEntityInitService
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(EnrichmentResultService::class, OffenderRepositoryFacade::class, OffenderEntityInitService::class)
@Sql(
  scripts = ["classpath:sql/before-common.sql", "classpath:sql/before-new-hearing-search.sql"],
  config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED),
  executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
class DefendantEnrichmentRepositoryIntTest {
  private val fromDate = LocalDate.of(2026, 10, 9)
  private val toDate = LocalDate.of(2026, 10, 23)

  @Autowired
  lateinit var repository: DefendantEnrichmentRepository

  @Autowired
  lateinit var jdbcTemplate: JdbcTemplate

  @Autowired
  lateinit var service: EnrichmentResultService

  @Autowired
  lateinit var entityManager: TestEntityManager

  @Autowired
  lateinit var defendantRepository: DefendantRepository

  @Autowired
  lateinit var groupedOffenderMatchRepository: GroupedOffenderMatchRepository

  @BeforeEach
  fun setUpTargets() {
    jdbcTemplate.update(
      """
        UPDATE hearing_day SET hearing_day = CASE
          WHEN id IN (6603, 6570, 6558) THEN DATE '2026-10-09'
          WHEN id IN (6569, 6556, 6559, 6563) THEN DATE '2026-10-22'
          WHEN id = 6557 THEN DATE '2026-10-08'
          ELSE DATE '2026-10-23'
        END
      """.trimIndent(),
    )
    jdbcTemplate.update(
      """
        UPDATE defendant SET c_id = CASE
          WHEN id IN (5812, 5837) THEN 'c12345'
          WHEN id = 5813 THEN 'c67890'
          WHEN id = 5838 THEN ''
          WHEN id = 5839 THEN '   '
          WHEN id = 5093 THEN 'common-platform-cid'
          ELSE NULL
        END
      """.trimIndent(),
    )
    jdbcTemplate.update("UPDATE defendant SET c_id = ? WHERE id = 5844", "\t\n")
  }

  @Test
  fun `should return common platform defendant IDs not CIDs`() {
    val result = repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, "B14LO")

    assertThat(result).containsExactly("7cece15c-78e8-4be9-a509-35d74eb68839")
  }

  @Test
  fun `should exclude manually updated common platform defendants`() {
    jdbcTemplate.update("UPDATE defendant SET manual_update = true WHERE id = 5093")

    val result = repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, "B14LO")

    assertThat(result).isEmpty()
  }

  @Test
  fun `should deduplicate libra CIDs across hearings and defendants and exclude blank CIDs`() {
    val result = repository.findLibraEnrichmentCandidates(fromDate, toDate, "B14LO")

    assertThat(result).containsExactly("c12345")
  }

  @Test
  fun `should exclude manually updated libra defendants`() {
    jdbcTemplate.update("UPDATE defendant SET manual_update = true WHERE c_id = ?", "c12345")

    val result = repository.findLibraEnrichmentCandidates(fromDate, toDate, "B14LO")

    assertThat(result).isEmpty()
  }

  @Test
  fun `should include the start date and exclude the end date for both sources`() {
    assertThat(repository.findLibraEnrichmentCandidates(fromDate, toDate, "B14LO"))
      .containsExactly("c12345")
    assertThat(repository.findLibraEnrichmentCandidates(toDate, toDate.plusDays(1), "B14LO"))
      .containsExactly("c67890")
    assertThat(repository.findCommonPlatformEnrichmentCandidates(fromDate, fromDate.plusDays(1), "B14LO"))
      .containsExactly("7cece15c-78e8-4be9-a509-35d74eb68839")
    assertThat(repository.findCommonPlatformEnrichmentCandidates(fromDate.minusDays(1), fromDate, "B14LO"))
      .isEmpty()
  }

  @Test
  fun `should return all distinct CIDs in deterministic order`() {
    val result = repository.findLibraEnrichmentCandidates(fromDate, toDate.plusDays(1), "B14LO")

    assertThat(result).containsExactly("c12345", "c67890")
  }

  @Test
  fun `should return all distinct common platform IDs despite repeated hearing days`() {
    jdbcTemplate.update("UPDATE court_case SET source_type = 'COMMON_PLATFORM' WHERE id = 7699")

    val result = repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, "B14LO")

    assertThat(result).containsExactly("7cece15c-78e8-4be9-a509-35d74eb68839", "8dc4322f-75de-429b-875b-0063b7c0c044")
  }

  @Test
  fun `should return more than 500 candidates for each source`() {
    jdbcTemplate.update(
      """
        INSERT INTO defendant (
          id, defendant_name, type, name, sex, created, created_by, manual_update, defendant_id,
          offender_confirmed, person_id, last_updated, last_updated_by, version, deleted, c_id
        )
        SELECT -100000 - n, 'Test Defendant', 'PERSON', '{"surname":"Defendant"}'::jsonb, 'NOT_KNOWN',
          now(), 'test', false, md5('defendant-' || n)::uuid, false, md5('person-' || n)::uuid,
          now(), 'test', 0, false, 'bulk-cid-' || n
        FROM generate_series(1, 501) n
      """.trimIndent(),
    )
    jdbcTemplate.update(
      """
        INSERT INTO hearing_defendant (
          id, fk_hearing_id, created, created_by, defendant_id, fk_defendant_id,
          last_updated, last_updated_by, version, deleted, outcome_not_required
        )
        SELECT -200000 - n * 2 - source, CASE source WHEN 0 THEN 5002 ELSE 5059 END,
          now(), 'test', md5('defendant-' || n)::uuid, -100000 - n, now(), 'test', 0, false, false
        FROM generate_series(1, 501) n CROSS JOIN generate_series(0, 1) source
      """.trimIndent(),
    )

    assertThat(repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, "B14LO")).hasSize(502)
    assertThat(repository.findLibraEnrichmentCandidates(fromDate, toDate, "B14LO")).hasSize(502)
  }

  @Test
  fun `should filter by court code on the same matching hearing day`() {
    jdbcTemplate.update("UPDATE hearing_day SET court_code = 'B10JQ' WHERE id IN (6603, 6571)")

    assertThat(repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, "B10JQ"))
      .containsExactly("7cece15c-78e8-4be9-a509-35d74eb68839")
    assertThat(repository.findLibraEnrichmentCandidates(fromDate, toDate, "B10JQ")).isEmpty()
    assertThat(repository.findLibraEnrichmentCandidates(fromDate, toDate.plusDays(1), "B10JQ"))
      .containsExactly("c67890")
    assertThat(repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, "B14LO")).isEmpty()
    assertThat(repository.findLibraEnrichmentCandidates(fromDate, toDate, "B14LO"))
      .containsExactly("c12345")
  }

  @Test
  fun `should return empty lists when no hearings match`() {
    val cp = repository.findCommonPlatformEnrichmentCandidates(fromDate, toDate, "UNKNOWN")
    val libra = repository.findLibraEnrichmentCandidates(fromDate, toDate, "UNKNOWN")

    assertThat(cp).isEmpty()
    assertThat(libra).isEmpty()
  }

  @Test
  fun `should resolve targets by source and include manual updates so stale results can be rejected`() {
    val defendantId = "7cece15c-78e8-4be9-a509-35d74eb68839"
    jdbcTemplate.update("UPDATE defendant SET manual_update = true WHERE id = 5093")

    assertThat(repository.findDefendantsForEnrichment(SourceType.COMMON_PLATFORM, defendantId).single().isManualUpdate).isTrue()
    assertThat(repository.findDefendantsForEnrichment(SourceType.LIBRA, defendantId)).isEmpty()
    assertThat(repository.findDefendantsForEnrichment(SourceType.COMMON_PLATFORM, "common-platform-cid")).isEmpty()
    assertThat(repository.findDefendantsForEnrichment(SourceType.LIBRA, "c12345").map { it.id }).containsExactly(5812L, 5837L)
  }

  @Test
  fun `should resolve a numeric libra CID without comparing it to the defendant UUID`() {
    jdbcTemplate.update("UPDATE defendant SET c_id = '236999322' WHERE id = 5813")

    assertThat(repository.findDefendantsForEnrichment(SourceType.LIBRA, "236999322").map { it.id }).containsExactly(5813L)
  }

  @Test
  fun `should exclude deleted targets and deleted hearing associations`() {
    jdbcTemplate.update("UPDATE defendant SET deleted = true WHERE id = 5093")
    jdbcTemplate.update("UPDATE hearing_defendant SET deleted = true WHERE fk_defendant_id = 5812")
    jdbcTemplate.update("UPDATE court_case SET deleted = true WHERE id = 7723")

    assertThat(repository.findDefendantsForEnrichment(SourceType.COMMON_PLATFORM, "7cece15c-78e8-4be9-a509-35d74eb68839")).isEmpty()
    assertThat(repository.findDefendantsForEnrichment(SourceType.LIBRA, "c12345")).isEmpty()
  }

  @Test
  fun `should persist defendant offender and match data across every libra target and support retries`() {
    val request = EnrichmentResultRequest(
      sourceType = EnrichmentSourceType.LIBRA,
      targetId = "c12345",
      status = EnrichmentStatus.ENRICHED,
      defendant = EnrichedDefendantDetails(cprUUID = "cpr-id", name = EnrichedName(forename1 = "Updated")),
      offender = EnrichedOffenderDetails(crn = "X999999", pnc = "offender-pnc"),
      probation = EnrichedProbationDetails(probationStatus = "CURRENT", breach = true),
      groupedOffenderMatches = EnrichedGroupedOffenderMatches(listOf(EnrichedOffenderMatch(EnrichedMatchIdentifiers("X999999")))),
      enrichedBy = "worker",
    )
    service.persistEnrichmentResult(request)
    entityManager.flush()
    entityManager.clear()
    service.persistEnrichmentResult(request)
    entityManager.flush()
    entityManager.clear()

    val first = defendantRepository.findById(5812L).orElseThrow()
    val second = defendantRepository.findById(5837L).orElseThrow()
    assertThat(first.cprUUID).isEqualTo("cpr-id")
    assertThat(second.cprUUID).isEqualTo("cpr-id")
    assertThat(first.defendantName).isEqualTo("Mr Updated Blogs")
    assertThat(first.pnc).isEqualTo("2004/0046583U")
    assertThat(first.offender.id).isEqualTo(second.offender.id)
    assertThat(first.offender.crn).isEqualTo("X999999")
    assertThat(first.offender.pnc).isEqualTo("offender-pnc")
    assertThat(first.offender.probationStatus).isEqualTo(OffenderProbationStatus.CURRENT)
    assertThat(first.offender.isBreach).isTrue()
    assertThat(first.isManualUpdate).isFalse()
    assertThat(first.isOffenderConfirmed).isFalse()
    val matches = groupedOffenderMatchRepository.findByCaseIdAndDefendantId("665597e6-b4d0-466d-98ad-3a9e9eb99b87", first.defendantId).orElseThrow()
    assertThat(matches.offenderMatches.map { it.crn }).containsExactly("X999999")
    assertThat(groupedOffenderMatchRepository.findByCaseIdAndDefendantId("85f400a9-82c9-4a9d-93ec-066d55be0c07", second.defendantId)).isPresent()
    assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM offender WHERE crn = 'X999999'", Long::class.java)).isEqualTo(1L)
  }

  @Test
  fun `should store one match per CRN when a new match group receives duplicate candidates`() {
    jdbcTemplate.update("DELETE FROM offender_match_group g USING defendant d WHERE d.defendant_id = g.defendant_id AND d.id IN (5812, 5837)")

    service.persistEnrichmentResult(
      EnrichmentResultRequest(
        sourceType = EnrichmentSourceType.LIBRA,
        targetId = "c12345",
        status = EnrichmentStatus.NEEDS_REVIEW,
        groupedOffenderMatches = EnrichedGroupedOffenderMatches(
          listOf("X111111", "X222222", "x111111 ").map { EnrichedOffenderMatch(EnrichedMatchIdentifiers(it)) },
        ),
        enrichedBy = "worker",
      ),
    )
    entityManager.flush()

    val crns = jdbcTemplate.queryForList(
      "SELECT m.crn FROM offender_match m JOIN offender_match_group g ON g.id = m.group_id JOIN defendant d ON d.defendant_id = g.defendant_id WHERE d.id = 5812 ORDER BY m.crn",
      String::class.java,
    )
    assertThat(crns).containsExactly("X111111", "X222222")
  }

  @Test
  fun `should leave all targets unchanged when one sharing the CID is manually updated`() {
    jdbcTemplate.update("UPDATE defendant SET manual_update = true WHERE id = 5837")

    assertThatThrownBy {
      service.persistEnrichmentResult(
        EnrichmentResultRequest(
          sourceType = EnrichmentSourceType.LIBRA,
          targetId = "c12345",
          status = EnrichmentStatus.ENRICHED,
          defendant = EnrichedDefendantDetails(cprUUID = "must-not-save"),
          enrichedBy = "worker",
        ),
      )
    }.isInstanceOf(ResponseStatusException::class.java)

    assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM defendant WHERE cpr_uuid = 'must-not-save'", Long::class.java)).isZero()
  }
}
