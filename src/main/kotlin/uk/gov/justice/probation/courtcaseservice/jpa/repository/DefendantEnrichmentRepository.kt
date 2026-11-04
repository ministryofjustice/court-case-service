package uk.gov.justice.probation.courtcaseservice.jpa.repository

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import java.time.LocalDate

interface DefendantEnrichmentRepository : Repository<DefendantEntity, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
    """
      SELECT d FROM DefendantEntity d
      WHERE d.deleted = false
        AND ((:sourceType = uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.COMMON_PLATFORM AND d.defendantId = :targetId)
          OR (:sourceType = uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.LIBRA AND d.cId = :targetId))
        AND EXISTS (
          SELECT hd.id FROM HearingDefendantEntity hd
          JOIN hd.hearing h
          JOIN h.courtCase c
          WHERE hd.defendant = d AND c.sourceType = :sourceType
            AND hd.deleted = false AND h.deleted = false AND c.deleted = false
        )
      ORDER BY d.id
    """,
  )
  fun findDefendantsForEnrichment(sourceType: SourceType, targetId: String): List<DefendantEntity>

  @Query(
    value = """
      SELECT DISTINCT d.defendantId
      FROM DefendantEntity d
      JOIN d.hearingDefendants hd
      JOIN hd.hearing h
      JOIN h.courtCase c
      JOIN h.hearingDays day
      WHERE c.sourceType = uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.COMMON_PLATFORM
        AND d.manualUpdate = false
        AND day.day >= :fromDate AND day.day < :toDate
        AND (CAST(:courtCode AS string) IS NULL OR day.courtCode = :courtCode)
      ORDER BY d.defendantId
    """,
    countQuery = """
      SELECT COUNT(DISTINCT d.defendantId)
      FROM DefendantEntity d
      JOIN d.hearingDefendants hd
      JOIN hd.hearing h
      JOIN h.courtCase c
      JOIN h.hearingDays day
      WHERE c.sourceType = uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.COMMON_PLATFORM
        AND d.manualUpdate = false
        AND day.day >= :fromDate AND day.day < :toDate
        AND (CAST(:courtCode AS string) IS NULL OR day.courtCode = :courtCode)
    """,
  )
  fun findCommonPlatformEnrichmentCandidates(
    fromDate: LocalDate,
    toDate: LocalDate,
    courtCode: String?,
    pageable: Pageable,
  ): Page<String>

  @Query(
    value = """
      SELECT DISTINCT d.cId
      FROM DefendantEntity d
      JOIN d.hearingDefendants hd
      JOIN hd.hearing h
      JOIN h.courtCase c
      JOIN h.hearingDays day
      WHERE c.sourceType = uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.LIBRA
        AND d.manualUpdate = false
        AND d.cId IS NOT NULL AND FUNCTION('regexp_replace', d.cId, '[[:space:]]', '', 'g') <> ''
        AND day.day >= :fromDate AND day.day < :toDate
        AND (CAST(:courtCode AS string) IS NULL OR day.courtCode = :courtCode)
      ORDER BY d.cId
    """,
    countQuery = """
      SELECT COUNT(DISTINCT d.cId)
      FROM DefendantEntity d
      JOIN d.hearingDefendants hd
      JOIN hd.hearing h
      JOIN h.courtCase c
      JOIN h.hearingDays day
      WHERE c.sourceType = uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.LIBRA
        AND d.manualUpdate = false
        AND d.cId IS NOT NULL AND FUNCTION('regexp_replace', d.cId, '[[:space:]]', '', 'g') <> ''
        AND day.day >= :fromDate AND day.day < :toDate
        AND (CAST(:courtCode AS string) IS NULL OR day.courtCode = :courtCode)
    """,
  )
  fun findLibraEnrichmentCandidates(
    fromDate: LocalDate,
    toDate: LocalDate,
    courtCode: String?,
    pageable: Pageable,
  ): Page<String>
}
