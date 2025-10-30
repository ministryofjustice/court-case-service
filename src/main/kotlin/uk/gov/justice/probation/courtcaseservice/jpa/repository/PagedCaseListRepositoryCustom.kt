package uk.gov.justice.probation.courtcaseservice.jpa.repository

import jakarta.persistence.EntityManager
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingSearchRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingStatus
import uk.gov.justice.probation.courtcaseservice.jpa.dto.CourtCaseDTO
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingDTO
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingDefendantDTO
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession.MORNING

@Repository
class PagedCaseListRepositoryCustom(private val entityManager: EntityManager) {

  companion object {

    private const val P_COURT_ROOM = "courtRoom"
    private const val P_PROBATION_STATUS = "probationStatus"
    private const val P_SOURCE = "source"
    private const val P_COURT_CODE = "courtCode"
    private const val P_DATE = "date"

    private const val JOIN_OFFENDER = "left join offender o on d.fk_offender_id = o.id "

    private const val BASE_WHERE = "where h.deleted is false "

    private const val MATCHES_JOIN = """
            left join (select uuid(omg1.defendant_id) as did, omg1.case_id as cid, count(omg1.defendant_id) as match_count
            from hearing_day hday1
            join hearing_defendant hd1 on hd1.fk_hearing_id = hday1.fk_hearing_id and hday1.hearing_day = :$P_DATE and hday1.court_code = :$P_COURT_CODE
            join defendant d1 on hd1.fk_defendant_id = d1.id
            join offender_match_group omg1 on omg1.defendant_id = text(hd1.defendant_id)
            join offender_match om1 on om1.group_id = omg1.id
           group by omg1.defendant_id, omg1.case_id) matches_group on text(hd.defendant_id) = text(matches_group.did) and cc.case_id = matches_group.cid   
        """

    private const val HEARING_JOIN = "join hearing h on hd.fk_hearing_id = h.id "
    private const val COURT_CASE_JOIN = "join court_case cc on h.fk_court_case_id = cc.id "
    private const val BASE_JOINS = """
                join hearing_defendant hd on hday.hearing_day = :$P_DATE and hday.court_code = :$P_COURT_CODE and hd.fk_hearing_id = hday.fk_hearing_id 
                join defendant d on d.id = hd.fk_defendant_id
            """
    private const val PROBATION_STATUS_FILTER = """
            and case        
                when (d.fk_offender_id is not null) then o.probation_status         
                when (d.fk_offender_id is null and d.offender_confirmed is false and matches_group.match_count > 0) then 'Possible NDelius record'        
                else 'NO_RECORD' 
            end in (:$P_PROBATION_STATUS)    
            """

    private const val ORDER_BY = """order by hday.court_room, hday.hearing_time, text(json(d."name")->'surname') """
  }

  @Transactional
  fun filterHearings(
    courtCode: String,
    hearingSearchRequest: HearingSearchRequest,
  ): PageImpl<Pair<HearingDefendantDTO, Int?>> {
    val pageable: Pageable = Pageable.ofSize(hearingSearchRequest.size).withPage(if (hearingSearchRequest.page > 0) hearingSearchRequest.page - 1 else 0)

    val hasProbationStatusFilter = hearingSearchRequest.probationStatus.isNotEmpty()
    val hasSourceFilter = hearingSearchRequest.source.size == 1
    val hasCourtRoom = hearingSearchRequest.courtRoom.isNotEmpty()

    var session = ""
    val sessionFilter = hearingSearchRequest.session
    if (sessionFilter != null && sessionFilter.size == 1) {
      session = """ and extract(hour from hday.hearing_time) ${ if (sessionFilter[0] == MORNING) " < 12 " else " >= 12 "} """
    }

    val hearingStatusFilter = """
            ${ if (hearingSearchRequest.hearingStatus == HearingStatus.HEARD) " inner join hearing_outcome ho on ho.fk_hearing_defendant_id = hd.id and ho.outcome_type != 'NO_OUTCOME' " else ""}
            ${ if (hearingSearchRequest.hearingStatus == HearingStatus.UNHEARD) " left join hearing_outcome ho on ho.fk_hearing_defendant_id = hd.id " else ""}
    """.trimIndent()

    val hasHearingOutcomeNotRequiredFilter = hearingSearchRequest.hearingOutcomeNotRequired != null
    val hearingOutcomeNotRequired = """
      ${ if (hasHearingOutcomeNotRequiredFilter && hearingSearchRequest.hearingOutcomeNotRequired == true) " and hd.outcome_not_required is true " else ""}
      ${ if (hasHearingOutcomeNotRequiredFilter && hearingSearchRequest.hearingOutcomeNotRequired == false) " and hd.outcome_not_required is false " else ""}
      ${ if (!hasHearingOutcomeNotRequiredFilter) " and hd.outcome_not_required is false " else ""}
    """.trimIndent()

    val joins = """
                $BASE_JOINS
                $HEARING_JOIN 
                    ${ if (hearingSearchRequest.recentlyAdded) " and date(h.first_created) = date(now()) " else ""}
                    $hearingStatusFilter
                $COURT_CASE_JOIN
                ${if (hasProbationStatusFilter || hearingSearchRequest.breach) JOIN_OFFENDER else ""}
    """.trimIndent()

    val filters = """
            ${ if (hasProbationStatusFilter) PROBATION_STATUS_FILTER else ""}
            ${ if (hasCourtRoom) " and hday.court_room in (:$P_COURT_ROOM)" else "" }
            ${ if (hasSourceFilter) " and cc.source_type = :$P_SOURCE" else "" }
            ${ if (hearingSearchRequest.breach) " and o.breach is true " else ""}
            ${ if (hearingSearchRequest.hearingStatus == HearingStatus.UNHEARD) " and ( ho.fk_hearing_defendant_id is null OR ho.outcome_type = 'NO_OUTCOME' ) " else ""}
            $hearingOutcomeNotRequired
            $session
    """.trimIndent()

    val coreSql = """
            from hearing_day hday
            $joins
            $MATCHES_JOIN
            $BASE_WHERE
            $filters
            
    """.trimIndent()
    val mainQuery = """
            select hd.*, matches_group.match_count as match_count  
            $coreSql
            $ORDER_BY
    """.trimIndent()

    val countQuery = """
            select count(hd.id)
            $coreSql
            $filters
    """.trimIndent()

    val mainJpaQuery = entityManager.createNativeQuery(mainQuery, "search_hearings_custom")
    val countJpaQuery = entityManager.createNativeQuery(countQuery)

    mainJpaQuery.setParameter(P_COURT_CODE, courtCode)
    mainJpaQuery.setParameter(P_DATE, hearingSearchRequest.date)

    countJpaQuery.setParameter(P_COURT_CODE, courtCode)
    countJpaQuery.setParameter(P_DATE, hearingSearchRequest.date)

    if (hasSourceFilter) {
      val source = hearingSearchRequest.source[0].name
      mainJpaQuery.setParameter(P_SOURCE, source)
      countJpaQuery.setParameter(P_SOURCE, source)
    }

    if (hasProbationStatusFilter) {
      val probationStatuses = hearingSearchRequest.probationStatus
      mainJpaQuery.setParameter(P_PROBATION_STATUS, probationStatuses)
      countJpaQuery.setParameter(P_PROBATION_STATUS, probationStatuses)
    }

    if (hasCourtRoom) {
      mainJpaQuery.setParameter(P_COURT_ROOM, hearingSearchRequest.courtRoom)
      countJpaQuery.setParameter(P_COURT_ROOM, hearingSearchRequest.courtRoom)
    }

    mainJpaQuery.firstResult = pageable.pageNumber * pageable.pageSize
    mainJpaQuery.maxResults = pageable.pageSize

    val result = mainJpaQuery.resultList
    val count = (countJpaQuery.singleResult as Long)

    val content = result.map { (it as Array<Any>) }.map { Pair(getHearingDefendantDTO(it[0] as HearingDefendantDTO), it[1] as Int?) }

    return PageImpl(content, pageable, count)
  }

  private fun getHearingDefendantDTO(hearingDefendantDto: HearingDefendantDTO): HearingDefendantDTO {
    addOffences(hearingDefendantDto)
    addHearingAndCourtCase(hearingDefendantDto)
    addDefendant(hearingDefendantDto)
    return hearingDefendantDto
  }

  private fun addDefendant(
    hdDTO: HearingDefendantDTO,
  ) {
    val hdDTO2WithDefendant = entityManager.createQuery(
      "select hd from HearingDefendantDTO hd JOIN FETCH hd.defendant ho where hd.id = :hearingDefendantId",
      HearingDefendantDTO::class.java,
    ).setParameter("hearingDefendantId", hdDTO.id).resultList

    if (hdDTO2WithDefendant.isNotEmpty()) {
      hdDTO.defendant = hdDTO2WithDefendant.first().defendant
    } else {
      hdDTO.defendant = null
    }
  }

  private fun addOffences(
    hdDTO: HearingDefendantDTO,
  ) {
    val hdDTOWithOffences = entityManager.createQuery(
      "select hd from HearingDefendantDTO hd JOIN FETCH hd.offences ho where hd.id = :hearingDefendantId",
      HearingDefendantDTO::class.java,
    ).setParameter("hearingDefendantId", hdDTO.id).resultList

    if (hdDTOWithOffences.isNotEmpty()) {
      hdDTO.offences = hdDTOWithOffences.first().offences
    } else {
      hdDTO.offences = emptyList()
    }
  }

  private fun addHearingAndCourtCase(hdDTO: HearingDefendantDTO) {
    val hearingDTOWithHearingDays = entityManager.createQuery(
      "select h from HearingDTO h JOIN FETCH h.hearingDays hd where h.id = :hearingId",
      HearingDTO::class.java,
    ).setParameter("hearingId", hdDTO.hearing.id).resultList

    if (hearingDTOWithHearingDays.isNotEmpty()) {
      val hearingDTO = hearingDTOWithHearingDays.first()
      val courtCaseDTO = entityManager.createQuery(
        "select cc from CourtCaseDTO cc JOIN FETCH cc.caseMarkers cm where cc.id = :courtCaseId",
        CourtCaseDTO::class.java,
      ).setParameter("courtCaseId", hearingDTO.courtCase.id).resultList

      if (courtCaseDTO.isNotEmpty()) {
        hearingDTO.courtCase = courtCaseDTO.first()
      } else {
        hearingDTO.courtCase.caseMarkers = emptyList()
      }
      hdDTO.hearing = hearingDTO
    } else {
      hdDTO.hearing = null
    }
  }
}
