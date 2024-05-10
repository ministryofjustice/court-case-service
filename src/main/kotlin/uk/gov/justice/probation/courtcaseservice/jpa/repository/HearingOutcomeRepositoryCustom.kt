package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSearchRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSortFields.HEARING_DATE
import java.time.LocalDate
import jakarta.persistence.EntityManager
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity

@Repository
class HearingOutcomeRepositoryCustom(
    private val entityManager: EntityManager,
    @Value("\${hearing-outcomes.resultedCasesDaysOffset:14}") private val resultedCasesDaysOffset: Long = 14
) {

    fun findByCourtCodeAndHearingOutcome(
        courtCode: String,
        hearingOutcomeSearchRequest: HearingOutcomeSearchRequest
    ): PageImpl<Pair<HearingDefendantEntity, LocalDate>> {
        val pageable: Pageable = Pageable.ofSize(hearingOutcomeSearchRequest.size).withPage(if (hearingOutcomeSearchRequest.page > 0) hearingOutcomeSearchRequest.page - 1 else 0)

        val filterBuilder = StringBuilder()

        val queryParams = LinkedHashMap<String, Any>()
        queryParams["courtCode"] = courtCode

        hearingOutcomeSearchRequest.state?.let {
            filterBuilder.append(" and ho.state = :state ")
            queryParams["state"] = it.name

            // a special case for resulted cases state to return only cases resulted within the last resultedCasesDaysOffset (default 14) days
            if(it == HearingOutcomeItemState.RESULTED) {
                filterBuilder.append(" and ho.resulted_date > :resultedDate ")
                queryParams["resultedDate"] = LocalDate.now().minusDays(resultedCasesDaysOffset).atTime(0,0,0)
            }
        }

        hearingOutcomeSearchRequest.assignedToUuid?.let {
            if (it.isNotEmpty()) {
                filterBuilder.append(" and assigned_to_uuid in (:assignedToUuid) ")
                queryParams["assignedToUuid"] = hearingOutcomeSearchRequest.assignedToUuid
            }
        }

        hearingOutcomeSearchRequest.outcomeType?.let { outcomeTypes ->
            if (outcomeTypes.isNotEmpty()) {
                filterBuilder.append("and outcome_type in (:outcomeTypes)")
                queryParams["outcomeTypes"] = hearingOutcomeSearchRequest.outcomeType.map { it.name }
            }
        }

        val orderByBuilder = StringBuilder(" order by ")
        if (hearingOutcomeSearchRequest.sortBy == null) {
            orderByBuilder.append("hday2.hearing_day")
        } else {
            // only sort by hearing date supported at the moment
            if (hearingOutcomeSearchRequest.sortBy == HEARING_DATE) {
                orderByBuilder.append("hday2.hearing_day ")
                hearingOutcomeSearchRequest.order?.let { orderByBuilder.append(it.name) }
            } else {
                orderByBuilder.append("hday2.hearing_day")
            }
        }

        val courtRoomParamName = "courtRoom"

        val hasCourtRoomFilter = hearingOutcomeSearchRequest.courtRoom.isNotEmpty()
        if(hasCourtRoomFilter) {
            queryParams[courtRoomParamName] = hearingOutcomeSearchRequest.courtRoom
        }

        val coreQuery = """
            from hearing_defendant hd 
            inner join hearing_outcome ho on ho.fk_hearing_defendant_id = hd.id
            $filterBuilder
            inner join
                (
                    select fk_hearing_id as hday_hearing_id, min(hearing_day) as hearing_day from hearing_day
                        where hearing_day.court_code = :courtCode
                        ${ if(hasCourtRoomFilter) " and hearing_day.court_room in (:$courtRoomParamName) " else "" }
                        group by fk_hearing_id
                ) hday2
            on hday2.hday_hearing_id = hd.fk_hearing_id	
                
            """

        val searchQuery = """
            select
            hd.*, hday2.hearing_day as hearing_day
            $coreQuery  
            $orderByBuilder
        """

        val countQuery = """
            select
            count(hd.id)
            $coreQuery  
        """

        val jpaQuery = entityManager.createNativeQuery(searchQuery, "search_hearing_outcomes_custom")
        val countJpaQuery = entityManager.createNativeQuery(countQuery)

        queryParams.entries.forEach {
            jpaQuery.setParameter(it.key, it.value)
            countJpaQuery.setParameter(it.key, it.value)
        }

        jpaQuery.firstResult  = pageable.pageNumber * pageable.pageSize
        jpaQuery.maxResults = pageable.pageSize

        val content = jpaQuery.resultList.map { it as Array<Any> }.map { Pair(it[0] as HearingDefendantEntity, it[1] as LocalDate) }
        val count = (countJpaQuery.singleResult as Long)

        return PageImpl(content, pageable, count)
    }

    fun getDynamicOutcomeCountsByState(courtCode: String): Map<String, Int> {
        val query = """
          select
            ho.state, count(ho.id) as count
            from hearing h 
            inner join hearing_defendant hd on hd.fk_hearing_id = h.id
            inner join hearing_outcome ho on ho.fk_hearing_defendant_id = hd.id 
            inner join
                (select fk_hearing_id as hday_hearing_id, min(hearing_day) as hearing_day from hearing_day where hearing_day.court_code = :courtCode group by fk_hearing_id) hday2
                on hday2.hday_hearing_id = h.id	    
            group by ho.state
        """.trimIndent()

        val jpaQuery = entityManager.createNativeQuery(query, "hearing_outcomes_by_state_count_custom")
        jpaQuery.setParameter("courtCode", courtCode)
        return jpaQuery.resultList.map { it as Pair<String, Int> }.associateBy({ it.first }, { it.second })
    }
}