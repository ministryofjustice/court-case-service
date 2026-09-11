package uk.gov.justice.probation.courtcaseservice.jpa.repository

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.probation.courtcaseservice.service.BailInformationReportRow
import java.time.LocalDate
import java.time.LocalTime

@Repository
class BailInformationServiceRepository(private val entityManager: EntityManager) {

  @Transactional(readOnly = true)
  fun fetchBailInformationReport(): List<BailInformationReportRow> {
    val query = """
      select
        h.hearing_id,
        h.hearing_event_type,
        h.hearing_type,
        hd.hearing_day,
        hd.hearing_time,
        o.summary,
        o.title,
        o.sequence,
        o.act,
        o.offence_code,
        o.plea_id,
        o.verdict_id,
        d.defendant_name,
        d.type,
        d.name,
        d.address,
        d.crn,
        d.pnc,
        d.cro,
        d.date_of_birth,
        d.sex,
        d.nationality_1,
        d.nationality_2,
        cc.urn,
        c.name,
        c.court_code
      from hearing h
      join hearing_defendant hdef on hdef.fk_hearing_id = h.id
      join hearing_day hd on hd.fk_hearing_id = h.id
      join offence o on hdef.id = o.fk_hearing_defendant_id
      join defendant d on hdef.defendant_id = d.defendant_id
      join court_case cc on h.fk_court_case_id = cc.id
      join court c on c.court_code = hd.court_code
      where lower(h.hearing_type) in ('bail application', 'bail variation application')
      and hd.hearing_day::date between current_date + 1 and current_date + 14
      order by hd.hearing_day asc
    """.trimIndent()

    @Suppress("UNCHECKED_CAST")
    return (entityManager.createNativeQuery(query).resultList as List<Array<Any?>>)
      .map { row ->
        BailInformationReportRow(
          hearingId = row[0] as String?,
          hearingEventType = row[1] as String?,
          hearingType = row[2] as String?,
          hearingDay = row[3] as LocalDate?,
          hearingTime = row[4] as LocalTime?,
          offenceSummary = row[5] as String?,
          offenceTitle = row[6] as String?,
          offenceSequence = row[7] as Int?,
          offenceAct = row[8] as String?,
          offenceCode = row[9] as String?,
          pleaId = row[10] as String?,
          verdictId = row[11] as String?,
          defendantName = row[12] as String?,
          defendantType = row[13] as String?,
          defendantNameJson = row[14] as String?,
          defendantAddress = row[15] as String?,
          crn = row[16] as String?,
          pnc = row[17] as String?,
          cro = row[18] as String?,
          dateOfBirth = row[19] as LocalDate?,
          sex = row[20] as String?,
          nationality1 = row[21] as String?,
          nationality2 = row[22] as String?,
          urn = row[23] as String?,
          courtName = row[24] as String?,
          courtCode = row[25] as String?,
        )
      }
  }
}
