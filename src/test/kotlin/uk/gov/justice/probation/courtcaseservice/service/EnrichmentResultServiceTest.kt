package uk.gov.justice.probation.courtcaseservice.service

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedAddress
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedAlias
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
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantEnrichmentRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepositoryFacade
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException
import uk.gov.justice.probation.courtcaseservice.service.model.MatchType
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class EnrichmentResultServiceTest {
  @Mock
  lateinit var enrichmentRepository: DefendantEnrichmentRepository

  @Mock
  lateinit var defendantRepository: DefendantRepository

  @Mock
  lateinit var offenderRepository: OffenderRepository

  @Mock
  lateinit var offenderRepositoryFacade: OffenderRepositoryFacade

  @Mock
  lateinit var groupedOffenderMatchRepository: GroupedOffenderMatchRepository

  lateinit var service: EnrichmentResultService

  private val appender = ListAppender<ILoggingEvent>()
  private val logger = LoggerFactory.getLogger(EnrichmentResultService::class.java) as Logger

  @BeforeEach
  fun setUp() {
    service = EnrichmentResultService(
      enrichmentRepository,
      defendantRepository,
      offenderRepository,
      offenderRepositoryFacade,
      groupedOffenderMatchRepository,
    )
    appender.start()
    logger.addAppender(appender)
  }

  @AfterEach
  fun tearDown() {
    logger.detachAppender(appender)
  }

  @Test
  fun `should log operational metadata and counts without personal data`() {
    givenDefendants(defendant())
    whenever(offenderRepositoryFacade.upsertOffender(any())).thenAnswer { it.arguments[0] }
    service.persistEnrichmentResult(
      EnrichmentResultRequest(
        sourceType = EnrichmentSourceType.COMMON_PLATFORM,
        targetId = "defendant-id",
        status = EnrichmentStatus.NEEDS_REVIEW,
        defendant = EnrichedDefendantDetails(
          cprUUID = "person-record-id",
          name = EnrichedName(forename1 = "Alex", surname = "Example"),
          dateOfBirth = LocalDate.of(1990, 1, 2),
          address = EnrichedAddress(line1 = "Building", postcode = "AB1 2CD"),
        ),
        offender = EnrichedOffenderDetails(crn = "X12345", pnc = "example-pnc", cro = "example-cro"),
        groupedOffenderMatches = EnrichedGroupedOffenderMatches(
          listOf(
            EnrichedOffenderMatch(
              EnrichedMatchIdentifiers(crn = "X12345", aliases = listOf(EnrichedAlias(firstName = "Alias"))),
              rejected = true,
            ),
            EnrichedOffenderMatch(EnrichedMatchIdentifiers(crn = "X67890")),
          ),
        ),
        warnings = listOf("warning text"),
        enrichedBy = "court-case-enrichment-worker",
        jobRunId = "job-run-uuid",
      ),
    )

    val message = appender.list.single().formattedMessage
    assertThat(message)
      .contains("sourceType=COMMON_PLATFORM", "targetId=defendant-id", "status=NEEDS_REVIEW", "jobRunId=job-run-uuid")
      .contains("hasDefendant=true", "hasOffender=true", "hasProbation=false")
      .contains("matchCount=2", "confirmedMatchCount=0", "rejectedMatchCount=1", "warningCount=1", "errorCount=0")
      .doesNotContain("person-record-id", "Alex", "Example", "1990", "AB1 2CD", "X12345", "X67890", "example-pnc", "example-cro", "Alias", "warning text")
  }

  @Test
  fun `should merge defendant details preserving court data and omitted fields`() {
    val defendant = defendant()
    givenDefendants(defendant)

    service.persistEnrichmentResult(
      request().copy(
        defendant = EnrichedDefendantDetails(
          cprUUID = "cpr-id",
          name = EnrichedName(forename1 = "Updated"),
          address = EnrichedAddress(postcode = "NEW"),
          sex = "Female",
          dateOfBirth = LocalDate.of(1991, 2, 3),
        ),
      ),
    )

    verify(defendantRepository).save(defendant)
    assertThat(defendant.cprUUID).isEqualTo("cpr-id")
    assertThat(defendant.name.forename1).isEqualTo("Updated")
    assertThat(defendant.name.forename3).isEqualTo("Third")
    assertThat(defendant.defendantName).isEqualTo("Updated Third Original")
    assertThat(defendant.address.line1).isEqualTo("Original street")
    assertThat(defendant.address.postcode).isEqualTo("NEW")
    assertThat(defendant.sex).isEqualTo(Sex.FEMALE)
    assertThat(defendant.dateOfBirth).isEqualTo(LocalDate.of(1991, 2, 3))
    assertThat(defendant.personId).isEqualTo("person-id")
    assertThat(defendant.pnc).isEqualTo("court-pnc")
    assertThat(defendant.cro).isEqualTo("court-cro")
    assertThat(defendant.type).isEqualTo(DefendantType.PERSON)
    assertThat(defendant.isManualUpdate).isFalse()
    assertThat(defendant.isOffenderConfirmed).isFalse()
    verifyNoInteractions(offenderRepositoryFacade, groupedOffenderMatchRepository)
  }

  @Test
  fun `should upsert and link an offender with probation details without treating it as a manual confirmation`() {
    val defendant = defendant()
    givenDefendants(defendant)
    whenever(offenderRepositoryFacade.upsertOffender(any())).thenAnswer { it.arguments[0] }

    service.persistEnrichmentResult(
      request().copy(
        offender = EnrichedOffenderDetails("X12345", "offender-pnc", "offender-cro"),
        probation = EnrichedProbationDetails("CURRENT", false, true, LocalDate.of(2025, 1, 1), true),
      ),
    )

    val captor = argumentCaptor<OffenderEntity>()
    verify(offenderRepositoryFacade).upsertOffender(captor.capture())
    assertThat(defendant.offender).isSameAs(captor.firstValue)
    assertThat(defendant.crn).isEqualTo("X12345")
    assertThat(defendant.offender.pnc).isEqualTo("offender-pnc")
    assertThat(defendant.offender.cro).isEqualTo("offender-cro")
    assertThat(defendant.offender.probationStatus).isEqualTo(OffenderProbationStatus.CURRENT)
    assertThat(defendant.offender.awaitingPsr).isFalse()
    assertThat(defendant.offender.isBreach).isTrue()
    assertThat(defendant.offender.isPreSentenceActivity).isTrue()
    assertThat(defendant.offender.previouslyKnownTerminationDate).isEqualTo(LocalDate.of(2025, 1, 1))
    assertThat(defendant.pnc).isEqualTo("court-pnc")
    assertThat(defendant.isManualUpdate).isFalse()
    assertThat(defendant.isOffenderConfirmed).isFalse()
  }

  @Test
  fun `should merge existing offender data and apply explicit false values`() {
    val offender = OffenderEntity.builder().crn("X12345").pnc("existing-pnc").cro("existing-cro")
      .probationStatus(OffenderProbationStatus.CURRENT).breach(true).preSentenceActivity(true)
      .awaitingPsr(true).suspendedSentenceOrder(true).build()
    val defendant = defendant().withOffender(offender)
    givenDefendants(defendant)
    whenever(offenderRepository.findByCrn("X12345")).thenReturn(Optional.of(offender))
    whenever(offenderRepositoryFacade.upsertOffender(any())).thenAnswer { it.arguments[0] }

    service.persistEnrichmentResult(request().copy(probation = EnrichedProbationDetails(breach = false, preSentenceActivity = false)))

    assertThat(defendant.offender.isBreach).isFalse()
    assertThat(defendant.offender.isPreSentenceActivity).isFalse()
    assertThat(defendant.offender.awaitingPsr).isTrue()
    assertThat(defendant.offender.isSuspendedSentenceOrder).isTrue()
    assertThat(defendant.offender.pnc).isEqualTo("existing-pnc")
    assertThat(defendant.offender.probationStatus).isEqualTo(OffenderProbationStatus.CURRENT)
  }

  @Test
  fun `should preserve an existing link when no match is returned`() {
    val offender = OffenderEntity.builder().crn("X12345").build()
    val defendant = defendant().withOffender(offender)
    givenDefendants(defendant)

    service.persistEnrichmentResult(request().copy(status = EnrichmentStatus.NO_MATCH))

    assertThat(defendant.offender).isSameAs(offender)
    verifyNoInteractions(offenderRepositoryFacade, groupedOffenderMatchRepository)
  }

  @Test
  fun `should update every defendant sharing a libra CID`() {
    val first = defendant()
    val second = defendant().withId(2L).withDefendantId("second-id")
    whenever(enrichmentRepository.findDefendantsForEnrichment(SourceType.LIBRA, "cid")).thenReturn(listOf(first, second))

    service.persistEnrichmentResult(
      request().copy(sourceType = EnrichmentSourceType.LIBRA, targetId = "cid", defendant = EnrichedDefendantDetails(cprUUID = "cpr-id")),
    )

    verify(defendantRepository).save(first)
    verify(defendantRepository).save(second)
    assertThat(first.cprUUID).isEqualTo("cpr-id")
    assertThat(second.cprUUID).isEqualTo("cpr-id")
  }

  @Test
  fun `should reject all updates if any target has been manually updated`() {
    givenDefendants(defendant(), defendant().withManualUpdate(true))

    assertThatThrownBy { service.persistEnrichmentResult(request()) }
      .isInstanceOf(ResponseStatusException::class.java)
      .satisfies({ assertThat((it as ResponseStatusException).statusCode).isEqualTo(HttpStatus.CONFLICT) })

    verifyNoInteractions(defendantRepository, offenderRepository, offenderRepositoryFacade, groupedOffenderMatchRepository)
  }

  @Test
  fun `should return not found for an unknown target`() {
    assertThatThrownBy { service.persistEnrichmentResult(request()) }
      .isInstanceOf(EntityNotFoundException::class.java)

    verifyNoInteractions(defendantRepository, offenderRepositoryFacade, groupedOffenderMatchRepository)
  }

  @Test
  fun `should reject probation details without an existing or supplied CRN`() {
    givenDefendants(defendant())

    assertThatThrownBy { service.persistEnrichmentResult(request().copy(probation = EnrichedProbationDetails(breach = true))) }
      .isInstanceOf(ResponseStatusException::class.java)
      .satisfies({ assertThat((it as ResponseStatusException).statusCode).isEqualTo(HttpStatus.BAD_REQUEST) })
    verifyNoInteractions(defendantRepository, offenderRepositoryFacade)
  }

  @Test
  fun `should persist grouped matches once per active case with aliases and identifiers`() {
    val defendant = defendantWithHearings()
    givenDefendants(defendant)

    service.persistEnrichmentResult(
      request().copy(
        groupedOffenderMatches = EnrichedGroupedOffenderMatches(
          listOf(
            EnrichedOffenderMatch(
              EnrichedMatchIdentifiers("X12345", "pnc", "cro", listOf(EnrichedAlias("Alias", "Surname", listOf("Middle")))),
              confirmed = true,
            ),
          ),
        ),
      ),
    )

    val captor = argumentCaptor<GroupedOffenderMatchesEntity>()
    verify(groupedOffenderMatchRepository).save(captor.capture())
    val group = captor.firstValue
    assertThat(group.caseId).isEqualTo("case-id")
    assertThat(group.defendantId).isEqualTo("defendant-id")
    val match = group.offenderMatches.single()
    assertThat(match.group).isSameAs(group)
    assertThat(match.crn).isEqualTo("X12345")
    assertThat(match.pnc).isEqualTo("pnc")
    assertThat(match.cro).isEqualTo("cro")
    assertThat(match.matchType).isEqualTo(MatchType.NAME_DOB_ALIAS)
    assertThat(match.confirmed).isTrue()
    assertThat(match.rejected).isFalse()
    assertThat(match.aliases.single().middleNames).containsExactly("Middle")
  }

  @Test
  fun `should preserve match decisions on retries and clear matches only for an explicit empty list`() {
    val defendant = defendantWithHearings()
    givenDefendants(defendant)
    val group = GroupedOffenderMatchesEntity.builder().caseId("case-id").defendantId("defendant-id")
      .offenderMatches(
        mutableListOf(
          OffenderMatchEntity.builder().crn("X12345").confirmed(true).rejected(false).build(),
        ),
      ).build()
    whenever(groupedOffenderMatchRepository.findByCaseIdAndDefendantId("case-id", "defendant-id")).thenReturn(Optional.of(group))
    val request = request().copy(
      groupedOffenderMatches = EnrichedGroupedOffenderMatches(listOf(EnrichedOffenderMatch(EnrichedMatchIdentifiers("X12345", pnc = "new-pnc")))),
    )

    service.persistEnrichmentResult(request)
    assertThat(group.offenderMatches.single().confirmed).isTrue()
    assertThat(group.offenderMatches.single().pnc).isEqualTo("new-pnc")

    service.persistEnrichmentResult(request.copy(groupedOffenderMatches = EnrichedGroupedOffenderMatches(emptyList())))
    assertThat(group.offenderMatches).isEmpty()
  }

  private fun request() = EnrichmentResultRequest(
    sourceType = EnrichmentSourceType.COMMON_PLATFORM,
    targetId = "defendant-id",
    status = EnrichmentStatus.ENRICHED,
    enrichedBy = "worker",
  )

  private fun givenDefendants(vararg defendants: DefendantEntity) {
    whenever(enrichmentRepository.findDefendantsForEnrichment(SourceType.COMMON_PLATFORM, "defendant-id")).thenReturn(defendants.toList())
  }

  private fun defendant(): DefendantEntity = DefendantEntity.builder()
    .id(1L).defendantId("defendant-id").personId("person-id")
    .name(NamePropertiesEntity.builder().forename1("Original").forename3("Third").surname("Original").build())
    .defendantName("Original Third Original")
    .address(AddressPropertiesEntity.builder().line1("Original street").postcode("OLD").build())
    .sex(Sex.MALE).type(DefendantType.PERSON).pnc("court-pnc").cro("court-cro")
    .hearingDefendants(emptyList()).build()

  private fun defendantWithHearings(): DefendantEntity {
    val courtCase = CourtCaseEntity.builder().caseId("case-id").sourceType(SourceType.COMMON_PLATFORM).build()
    val hearing = HearingEntity.builder().courtCase(courtCase).build()
    return defendant().withHearingDefendants(
      listOf(
        HearingDefendantEntity.builder().hearing(hearing).build(),
        HearingDefendantEntity.builder().hearing(hearing).build(),
        HearingDefendantEntity.builder().hearing(hearing).deleted(true).build(),
      ),
    )
  }
}
