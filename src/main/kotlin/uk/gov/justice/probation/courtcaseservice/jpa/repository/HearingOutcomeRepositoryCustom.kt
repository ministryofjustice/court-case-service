package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.springframework.stereotype.Repository
import uk.gov.justice.probation.courtcaseservice.controller.model.*
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import java.time.LocalDate
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Join
import javax.persistence.criteria.Predicate

@Repository
class HearingOutcomeRepositoryCustom(private val entityManager: EntityManager) {

    fun findByCourtCodeAndHearingOutcome2(
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
            if(outcomeTypes.isNotEmpty()) {
                filterBuilder.append("and outcome_type in (:outcomeTypes)")
            }
        }

        val orderByBuilder = StringBuilder(" order by ")
        if(hearingOutcomeSearchRequest.sortBy == null) {
            orderByBuilder.append("hday2.hearing_day")
        } else {
            // only sort by hearing date supported at the moment
            if (hearingOutcomeSearchRequest.sortBy == HearingOutcomeSortFields.HEARING_DATE) {
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
            if(outcomeTypes.isNotEmpty()) {
                jpaQuery.setParameter("outcomeTypes", hearingOutcomeSearchRequest.outcomeType.map { it.name })
            }
        }

        var result = jpaQuery.resultList.map {it as Array<Any>}.map {Pair(it[0] as HearingEntity, it[1] as LocalDate)}
        return result;
    }

    fun findByCourtCodeAndHearingOutcome(
        courtCode: String,
        hearingOutcomeSearchRequest: HearingOutcomeSearchRequest
    ): List<HearingEntity> {

        val criteriaBuilder = entityManager.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(HearingEntity::class.java)

        val root = criteriaQuery.distinct(true).from(HearingEntity::class.java)

        val hearingDay = root.join<HearingEntity, HearingDayEntity>("hearingDays")
        val hearingOutcome = root.join<HearingEntity, HearingOutcomeEntity>("hearingOutcome")

        val restrictions = mutableListOf<Predicate>(
            criteriaBuilder.equal(hearingDay.get<Predicate>("courtCode"), courtCode)
        )

        hearingOutcomeSearchRequest.state?.let {
            restrictions.add(criteriaBuilder.equal(hearingOutcome.get<Predicate>("state"), it.name))
        }

        hearingOutcomeSearchRequest.assignedToUuid?.let {
            restrictions.add(criteriaBuilder.equal(hearingOutcome.get<Predicate>("assignedToUuid"), it))
        }

        hearingOutcomeSearchRequest.outcomeType?.let { outcomeTypes ->
            if(outcomeTypes.isNotEmpty()) {
                restrictions.add(hearingOutcome.get<Predicate>("outcomeType").`in`(hearingOutcomeSearchRequest.outcomeType.map { it.name }))
            }
        }

        criteriaQuery.where(*restrictions.toTypedArray())

        processSorting(hearingOutcomeSearchRequest, hearingDay, criteriaQuery, criteriaBuilder)
        val resultList = entityManager.createQuery(criteriaQuery)
            .resultList
        return resultList
    }

    private fun processSorting(
        hearingOutcomeSearchRequest: HearingOutcomeSearchRequest,
        hearingDay: Join<HearingEntity, HearingDayEntity>,
        criteriaQuery: CriteriaQuery<HearingEntity>,
        criteriaBuilder: CriteriaBuilder
    ) {
        hearingOutcomeSearchRequest.sortBy?.let {
            if (it == HearingOutcomeSortFields.HEARING_DATE) {
                val sortField = hearingDay.get<Predicate>("day")
                criteriaQuery.orderBy(
                    if (hearingOutcomeSearchRequest.order == null || hearingOutcomeSearchRequest.order == SortOrder.ASC) criteriaBuilder.asc(
                        sortField
                    ) else criteriaBuilder.desc(sortField)
                )
            }
        }
    }
}