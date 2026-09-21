package uk.gov.justice.probation.courtcaseservice.database.seeders

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingPrepStatus
import uk.gov.justice.probation.courtcaseservice.controller.model.ScenarioDefendant
import uk.gov.justice.probation.courtcaseservice.controller.model.ScenarioHearingDay
import uk.gov.justice.probation.courtcaseservice.controller.model.SeedScenarioDocument
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseMarkerEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEventType
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.JudicialResultEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PhoneNumberEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PleaEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import uk.gov.justice.probation.courtcaseservice.jpa.entity.VerdictEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CaseCommentsRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDayRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNoteRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingOutcomeRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.JudicialResultRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenceRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.PleaRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.VerdictRepository
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Service
class ScenarioSeedService(
  private val entityManager: EntityManager,
  private val courtCaseRepository: CourtCaseRepository,
  private val hearingRepository: HearingRepository,
  private val hearingDayRepository: HearingDayRepository,
  private val hearingDefendantRepository: HearingDefendantRepository,
  private val hearingNoteRepository: HearingNoteRepository,
  private val defendantRepository: DefendantRepository,
  private val offenderRepository: OffenderRepository,
  private val caseCommentsRepository: CaseCommentsRepository,
  private val pleaRepository: PleaRepository,
  private val verdictRepository: VerdictRepository,
  private val offenceRepository: OffenceRepository,
  private val judicialResultRepository: JudicialResultRepository,
  private val hearingOutcomeRepository: HearingOutcomeRepository,
) {

  private fun hearingDayOrToday(day: ScenarioHearingDay): ScenarioHearingDay = day.copy(
    day = day.day ?: LocalDate.now(),
    time = day.time ?: LocalTime.of(9, 0),
    courtCode = day.courtCode ?: "B10JQ",
    courtRoom = day.courtRoom ?: "1",
  )

  private fun defendantName(defendant: ScenarioDefendant): String = defendant.defendantName
    ?: defendant.name?.let {
      listOfNotNull(it.forename1, it.forename2, it.forename3, it.surname)
       .joinToString(" ")
       .trim()
    }
    ?: "QA Defendant"

  private fun assignedUuid(
    value: String?,
    fallback: () -> String = { UUID.randomUUID().toString() },
  ): String = value?.takeIf { it.isNotBlank() } ?: fallback()

  private fun assignedUuidStateful(state: MutableMap<String, String>, key: String): String = state.getOrPut(key) { UUID.randomUUID().toString() }

  private fun defendantType(type: String?): DefendantType = runCatching {
    DefendantType.valueOf(type ?: "PERSON")
  }.getOrElse { DefendantType.PERSON }

  private fun sex(sex: String?): Sex = runCatching {
    Sex.valueOf(sex ?: "MALE")
  }.getOrElse { Sex.MALE }

  private fun hearingEventType(type: String?): HearingEventType? = runCatching {
    type?.let { HearingEventType.valueOf(it) }
  }.getOrNull()

  private fun hearingType(type: String?): String = when (type?.trim()?.lowercase()) {
    "sentence" -> "Sentence"
    else -> type ?: "Trial"
  }

  private fun deleteMatchingCases(document: SeedScenarioDocument) {
    document.cases.forEach { scenarioCase ->
      val caseNo = scenarioCase.caseNo ?: return@forEach
      val defendantNames = scenarioCase.defendants.map(::defendantName).toSet()
      val courtCodes = scenarioCase.hearings
        .flatMap { hearing -> hearing.hearingDays.ifEmpty { listOf(ScenarioHearingDay()) } }
        .map(::hearingDayOrToday)
        .mapNotNull { it.courtCode }
        .toSet()

      if (defendantNames.isEmpty() || courtCodes.isEmpty()) return@forEach

      val matchingIds = entityManager.createNativeQuery(
        """
          select distinct cc.id
          from court_case cc
          join hearing h on h.fk_court_case_id = cc.id
          join hearing_day hd on hd.fk_hearing_id = h.id
          join case_defendant cd on cd.fk_court_case_id = cc.id
          join defendant d on d.id = cd.fk_case_defendant_id
          where cc.case_no = :caseNo
            and hd.court_code in (:courtCodes)
            and d.defendant_name in (:defendantNames)
        """.trimIndent(),
      )
        .setParameter("caseNo", caseNo)
        .setParameter("courtCodes", courtCodes)
        .setParameter("defendantNames", defendantNames)
        .resultList
        .map { (it as Number).toLong() }

      if (matchingIds.isEmpty()) return@forEach

      val matchingDefendantIds = entityManager.createNativeQuery(
        """
          select distinct cd.fk_case_defendant_id
          from case_defendant cd
          where cd.fk_court_case_id in (:matchingIds)
        """.trimIndent(),
      )
        .setParameter("matchingIds", matchingIds)
        .resultList
        .map { (it as Number).toLong() }

      val matchingOffenderIds = if (matchingDefendantIds.isEmpty()) {
        emptyList()
      } else {
        entityManager.createNativeQuery(
          """
            select distinct d.fk_offender_id
            from defendant d
            where d.id in (:matchingDefendantIds)
              and d.fk_offender_id is not null
          """.trimIndent(),
        )
          .setParameter("matchingDefendantIds", matchingDefendantIds)
          .resultList
          .map { (it as Number).toLong() }
      }

      val matchingHearingIds = entityManager.createNativeQuery(
        """
          select distinct h.id
          from hearing h
          where h.fk_court_case_id in (:matchingIds)
        """.trimIndent(),
      )
        .setParameter("matchingIds", matchingIds)
        .resultList
        .map { (it as Number).toLong() }

      val matchingHearingDefendantIds = if (matchingHearingIds.isEmpty()) {
        emptyList()
      } else {
        entityManager.createNativeQuery(
          """
            select distinct hd.id
            from hearing_defendant hd
            where hd.fk_hearing_id in (:matchingHearingIds)
          """.trimIndent(),
        )
          .setParameter("matchingHearingIds", matchingHearingIds)
          .resultList
          .map { (it as Number).toLong() }
      }

      val matchingOffenceIds = if (matchingHearingDefendantIds.isEmpty()) {
        emptyList()
      } else {
        entityManager.createNativeQuery(
          """
            select distinct o.id
            from offence o
            where o.fk_hearing_defendant_id in (:matchingHearingDefendantIds)
          """.trimIndent(),
        )
          .setParameter("matchingHearingDefendantIds", matchingHearingDefendantIds)
          .resultList
          .map { (it as Number).toLong() }
      }

      val matchingCaseDefendantIds = entityManager.createNativeQuery(
        """
          select distinct cd.id
          from case_defendant cd
          where cd.fk_court_case_id in (:matchingIds)
        """.trimIndent(),
      )
        .setParameter("matchingIds", matchingIds)
        .resultList
        .map { (it as Number).toLong() }

      entityManager.createNativeQuery(
        """
          delete from case_comments
          where case_id in (
            select case_id from court_case where id in (:matchingIds)
          )
        """.trimIndent(),
      ).setParameter("matchingIds", matchingIds).executeUpdate()

      entityManager.createNativeQuery(
        """
          delete from case_marker
          where fk_court_case_id in (:matchingIds)
        """.trimIndent(),
      ).setParameter("matchingIds", matchingIds).executeUpdate()

      if (matchingHearingDefendantIds.isNotEmpty()) {
        entityManager.createNativeQuery(
          """
            delete from hearing_notes
            where fk_hearing_defendant_id in (:matchingHearingDefendantIds)
          """.trimIndent(),
        ).setParameter("matchingHearingDefendantIds", matchingHearingDefendantIds).executeUpdate()

        entityManager.createNativeQuery(
          """
            delete from hearing_outcome
            where fk_hearing_defendant_id in (:matchingHearingDefendantIds)
          """.trimIndent(),
        ).setParameter("matchingHearingDefendantIds", matchingHearingDefendantIds).executeUpdate()
      }

      if (matchingOffenceIds.isNotEmpty()) {
        entityManager.createNativeQuery(
          """
            delete from judicial_result
            where offence_id in (:matchingOffenceIds)
          """.trimIndent(),
        ).setParameter("matchingOffenceIds", matchingOffenceIds).executeUpdate()

        entityManager.createNativeQuery(
          """
            delete from offence
            where id in (:matchingOffenceIds)
          """.trimIndent(),
        ).setParameter("matchingOffenceIds", matchingOffenceIds).executeUpdate()
      }

      if (matchingHearingIds.isNotEmpty()) {
        entityManager.createNativeQuery(
          """
            delete from hearing_day
            where fk_hearing_id in (:matchingHearingIds)
          """.trimIndent(),
        ).setParameter("matchingHearingIds", matchingHearingIds).executeUpdate()
      }

      if (matchingHearingDefendantIds.isNotEmpty()) {
        entityManager.createNativeQuery(
          """
            delete from hearing_defendant
            where id in (:matchingHearingDefendantIds)
          """.trimIndent(),
        ).setParameter("matchingHearingDefendantIds", matchingHearingDefendantIds).executeUpdate()
      }

      if (matchingHearingIds.isNotEmpty()) {
        entityManager.createNativeQuery(
          """
            delete from hearing
            where id in (:matchingHearingIds)
          """.trimIndent(),
        ).setParameter("matchingHearingIds", matchingHearingIds).executeUpdate()
      }

      if (matchingCaseDefendantIds.isNotEmpty()) {
        entityManager.createNativeQuery(
          """
            delete from case_defendant_documents
            where fk_case_defendant_id in (:matchingCaseDefendantIds)
          """.trimIndent(),
        ).setParameter("matchingCaseDefendantIds", matchingCaseDefendantIds).executeUpdate()
      }

      entityManager.createNativeQuery(
        """
          delete from case_defendant
          where fk_court_case_id in (:matchingIds)
        """.trimIndent(),
      ).setParameter("matchingIds", matchingIds).executeUpdate()

      if (matchingDefendantIds.isNotEmpty()) {
        entityManager.createNativeQuery(
          """
            delete from defendant
            where id in (:matchingDefendantIds)
          """.trimIndent(),
        ).setParameter("matchingDefendantIds", matchingDefendantIds).executeUpdate()
      }

      if (matchingOffenderIds.isNotEmpty()) {
        entityManager.createNativeQuery(
          """
            delete from offender
            where id in (:matchingOffenderIds)
          """.trimIndent(),
        ).setParameter("matchingOffenderIds", matchingOffenderIds).executeUpdate()
      }

      entityManager.createNativeQuery(
        """
          delete from court_case
          where id in (:matchingIds)
        """.trimIndent(),
      ).setParameter("matchingIds", matchingIds).executeUpdate()
    }
  }

  @Transactional
  fun seed(document: SeedScenarioDocument) {
    if (document.clean) {
      deleteMatchingCases(document)
    }

    document.cases.forEach { scenarioCase ->
      val ids = mutableMapOf<String, String>()
      val defendantIds = mutableMapOf<ScenarioDefendant, String>()
      val caseEntity = CourtCaseEntity.builder()
        .caseId(assignedUuidStateful(ids, "caseId"))
        .caseNo(scenarioCase.caseNo ?: "CASE-${UUID.randomUUID().toString().takeLast(8)}")
        .urn(scenarioCase.urn ?: "")
        .sourceType(runCatching { SourceType.valueOf(scenarioCase.sourceType ?: "COMMON_PLATFORM") }.getOrDefault(SourceType.COMMON_PLATFORM))
        .hearings(mutableListOf())
        .caseMarkers(mutableListOf())
        .caseDefendants(mutableListOf())
        .build()

      if (scenarioCase.caseMarkers.isNotEmpty()) {
        val markers = scenarioCase.caseMarkers.map { marker ->
          CaseMarkerEntity.builder().typeDescription(marker).courtCase(caseEntity).build()
        }
        caseEntity.addCaseMarkers(markers)
      }

      courtCaseRepository.save(caseEntity)

      scenarioCase.comments.forEach { comment ->
        val entity = CaseCommentEntity.builder()
          .caseId(caseEntity.caseId)
          .defendantId(caseEntity.caseDefendants.firstOrNull()?.defendant?.defendantId ?: assignedUuidStateful(ids, "commentDefendantId"))
          .createdByUuid(assignedUuidStateful(ids, "commentCreatedByUuid"))
          .author(comment.author ?: "QA")
          .comment(comment.comment)
          .draft(comment.draft)
          .legacy(comment.legacy)
          .build()
        caseCommentsRepository.save(entity)
      }

      scenarioCase.defendants.forEach { scenarioDefendant ->
        val defendantId = assignedUuidStateful(ids, "defendantId-${defendantIds.size}")
        defendantIds[scenarioDefendant] = defendantId
        val offenderEntity = scenarioDefendant.offender?.let { offenderInput ->
          offenderRepository.save(
            OffenderEntity.builder()
              .crn(offenderInput.crn ?: scenarioDefendant.crn ?: "SCENARIO")
              .pnc(offenderInput.pnc ?: scenarioDefendant.pnc)
              .cro(offenderInput.cro ?: scenarioDefendant.cro)
              .probationStatus(offenderInput.probationStatus?.let { OffenderProbationStatus.of(it) })
              .awaitingPsr(offenderInput.awaitingPsr)
              .breach(offenderInput.breach)
              .preSentenceActivity(offenderInput.preSentenceActivity)
              .suspendedSentenceOrder(offenderInput.suspendedSentenceOrder)
              .previouslyKnownTerminationDate(offenderInput.previouslyKnownTerminationDate)
              .build(),
          )
        }

        val defendantEntity = DefendantEntity.builder()
          .defendantId(defendantId)
          .defendantName(defendantName(scenarioDefendant))
          .name(
            scenarioDefendant.name?.let {
              NamePropertiesEntity.builder()
                .title(it.title)
                .forename1(it.forename1)
                .forename2(it.forename2)
                .forename3(it.forename3)
                .surname(it.surname)
                .build()
            } ?: NamePropertiesEntity.builder()
              .forename1("QA")
              .surname("User")
              .build(),
          )
          .type(defendantType(scenarioDefendant.type))
          .sex(sex(scenarioDefendant.sex))
          .address(
            scenarioDefendant.address?.let {
              AddressPropertiesEntity.builder()
                .line1(it.line1)
                .line2(it.line2)
                .line3(it.line3)
                .line4(it.line4)
                .line5(it.line5)
                .postcode(it.postcode)
                .build()
            },
          )
          .phoneNumber(
            scenarioDefendant.phoneNumber?.let {
              PhoneNumberEntity.builder()
                .home(it.home)
                .mobile(it.mobile)
                .work(it.work)
                .build()
            },
          )
          .dateOfBirth(scenarioDefendant.dateOfBirth)
          .cro(scenarioDefendant.cro ?: offenderEntity?.cro)
          .crn(scenarioDefendant.crn ?: offenderEntity?.crn)
          .pnc(scenarioDefendant.pnc ?: offenderEntity?.pnc)
          .nationality1(scenarioDefendant.nationality1)
          .nationality2(scenarioDefendant.nationality2)
          .personId(assignedUuidStateful(ids, "personId"))
          .offender(offenderEntity)
          .build()

        val savedDefendant = defendantRepository.save(defendantEntity)
        caseEntity.addCaseDefendant(savedDefendant)
      }

      scenarioCase.hearings.forEach { scenarioHearing ->
        val hearingEntity = HearingEntity.builder()
          .hearingId(assignedUuidStateful(ids, "hearingId"))
          .courtCase(caseEntity)
          .hearingEventType(hearingEventType(scenarioHearing.hearingEventType))
          .hearingType(hearingType(scenarioHearing.hearingType))
          .listNo(scenarioHearing.listNo ?: "1")
          .hearingDays(mutableListOf())
          .hearingDefendants(mutableListOf())
          .build()

        scenarioHearing.hearingDays.ifEmpty {
          listOf(
            ScenarioHearingDay(
              day = LocalDate.now(),
              time = LocalTime.of(9, 0),
              courtCode = "B10JQ",
              courtRoom = "1",
            ),
          )
        }.map(::hearingDayOrToday).forEach { day ->
          val hearingDay = HearingDayEntity.builder()
            .hearing(hearingEntity)
            .day(day.day!!)
            .time(day.time!!)
            .courtCode(day.courtCode!!)
            .courtRoom(day.courtRoom!!)
            .build()
          hearingEntity.hearingDays.add(hearingDay)
          hearingDayRepository.save(hearingDay)
        }

        val savedHearing = hearingRepository.save(hearingEntity)

        scenarioHearing.defendants.forEach { hearingDefendantInput ->
          val hearingDefendantId = caseEntity.caseDefendants.firstOrNull()?.defendant?.defendantId
            ?: throw IllegalStateException("Scenario hearing defendant is missing a defendantId")

          val defendant = caseEntity.caseDefendants
            .firstOrNull { it.defendant.defendantId == hearingDefendantId }
            ?.defendant ?: defendantRepository.findFirstByDefendantId(hearingDefendantId).orElseThrow()

          val hearingDefendant = HearingDefendantEntity.builder()
            .hearing(savedHearing)
            .defendantId(defendant.defendantId)
            .defendant(defendant)
            .prepStatus(hearingDefendantInput.prepStatus ?: HearingPrepStatus.NOT_STARTED.name)
            .outcomeNotRequired(hearingDefendantInput.outcomeNotRequired ?: false)
            .offences(mutableListOf())
            .notes(mutableListOf())
            .build()

          hearingDefendantInput.notes.forEach { note ->
            val hearingNote = HearingNoteEntity.builder()
              .hearingId(savedHearing.hearingId)
              .hearingDefendant(hearingDefendant)
              .createdByUuid(assignedUuidStateful(ids, "noteCreatedByUuid"))
              .author(note.author ?: "QA")
              .note(note.note)
              .draft(note.draft)
              .legacy(note.legacy)
              .build()
            hearingDefendant.notes.add(hearingNote)
            hearingNoteRepository.save(hearingNote)
          }

          hearingDefendantInput.offences.forEach { offenceInput ->
            val pleaEntity = offenceInput.plea?.let {
              pleaRepository.save(PleaEntity.builder().value(it.value).date(it.date).build())
            }
            val verdictEntity = offenceInput.verdict?.let {
              verdictRepository.save(VerdictEntity.builder().typeDescription(it.typeDescription).date(it.date).build())
            }

            val offenceEntity = OffenceEntity.builder()
              .title(offenceInput.title)
              .summary(offenceInput.summary)
              .act(offenceInput.act)
              .sequence(offenceInput.sequence)
              .listNo(offenceInput.listNo)
              .judicialResults(mutableListOf())
              .offenceCode(offenceInput.offenceCode)
              .plea(pleaEntity)
              .verdict(verdictEntity)
              .build()

            offenceEntity.hearingDefendant = hearingDefendant
            offenceInput.judicialResults.forEach { jr ->
              val judicial = JudicialResultEntity.builder()
                .label(jr.label)
                .judicialResultTypeId(assignedUuidStateful(ids, "judicialResultTypeId"))
                .resultText(jr.resultText)
                .isConvictedResult(jr.isConvictedResult)
                .offence(offenceEntity)
                .build()
              offenceEntity.judicialResults.add(judicial)
            }

            hearingDefendant.offences.add(offenceEntity)
          }

          hearingDefendantRepository.save(hearingDefendant)
        }
      }
    }
  }
}
