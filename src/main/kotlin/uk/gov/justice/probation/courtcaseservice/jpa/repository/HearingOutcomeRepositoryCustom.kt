package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.springframework.stereotype.Repository
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSearchRequest
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSortFields.HEARING_DATE
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import java.time.LocalDate
import javax.persistence.EntityManager

@Repository
class HearingOutcomeRepositoryCustom(private val entityManager: EntityManager) {

    fun findByCourtCodeAndHearingOutcome(
        courtCode: String,
        hearingOutcomeSearchRequest: HearingOutcomeSearchRequest
    ): List<Pair<HearingEntity, LocalDate>> {

        var filterBuilder = StringBuilder();

        hearingOutcomeSearchRequest.state?.let {
            filterBuilder.append(" and ho.state = :state ")
        }

        hearingOutcomeSearchRequest.assignedToUuid?.let {
            filterBuilder.append(" and assigned_to_uuid = :assignedToUuid ")
        }

        hearingOutcomeSearchRequest.outcomeType?.let { outcomeTypes ->
            if (outcomeTypes.isNotEmpty()) {
                filterBuilder.append("and outcome_type in (:outcomeTypes)")
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

        val searchQuery = """
            select
            h.*, hday2.hearing_day as hearing_day
            from hearing h 
            inner join hearing_outcome ho on h.fk_hearing_outcome = ho.id 
                $filterBuilder
            inner join
                (select fk_hearing_id as hday_hearing_id, min(hearing_day) as hearing_day from hearing_day where hearing_day.court_code = :courtCode group by fk_hearing_id) hday2
                on hday2.hday_hearing_id = h.id	      
            $orderByBuilder
        """

        var jpaQuery = entityManager.createNativeQuery(searchQuery, "search_hearing_outcomes_custom")

        jpaQuery.setParameter("courtCode", courtCode)

        hearingOutcomeSearchRequest.state?.let {
            jpaQuery.setParameter("state", hearingOutcomeSearchRequest.state.name)
        }

        hearingOutcomeSearchRequest.assignedToUuid?.let {
            jpaQuery.setParameter("assignedToUuid", hearingOutcomeSearchRequest.assignedToUuid)
        }

        hearingOutcomeSearchRequest.outcomeType?.let { outcomeTypes ->
            if (outcomeTypes.isNotEmpty()) {
                jpaQuery.setParameter("outcomeTypes", hearingOutcomeSearchRequest.outcomeType.map { it.name })
            }
        }

        return jpaQuery.resultList.map { it as Array<Any> }.map { Pair(it[0] as HearingEntity, it[1] as LocalDate) };
    }

    fun getDynamicOutcomeCountsByState(courtCode: String): Map<String, Int> {
        var query = """
          select
            ho.state, count(ho.id) as count
            from hearing h 
            inner join hearing_outcome ho on h.fk_hearing_outcome = ho.id 
            inner join
                (select fk_hearing_id as hday_hearing_id, min(hearing_day) as hearing_day from hearing_day where hearing_day.court_code = :courtCode group by fk_hearing_id) hday2
                on hday2.hday_hearing_id = h.id	    
            group by ho.state
        """.trimIndent()

        var jpaQuery = entityManager.createNativeQuery(query, "hearing_outcomes_by_state_count_custom")
        jpaQuery.setParameter("courtCode", courtCode)
        return jpaQuery.resultList.map { it as Pair<String, Int> }.associateBy({ it.first }, { it.second })
    }
}