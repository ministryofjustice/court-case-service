package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.springframework.stereotype.Repository
import uk.gov.justice.probation.courtcaseservice.controller.model.*
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Join
import javax.persistence.criteria.Predicate

@Repository
class HearingOutcomeRepositoryCustom(private val entityManager: EntityManager) {

    fun findByCourtCodeAndHearingOutcome(
        courtCode: String,
        hearingOutcomeSearchRequest: HearingOutcomeSearchRequest
    ): List<HearingEntity> {

        val criteriaBuilder = entityManager.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(HearingEntity::class.java)

        val root = criteriaQuery.from(HearingEntity::class.java)

        val hearingDay = root.join<HearingEntity, HearingDayEntity>("hearingDays")
        val hearingOutcome = root.join<HearingEntity, HearingOutcomeEntity>("hearingOutcome")

        val restrictions = mutableListOf<Predicate>(
            criteriaBuilder.equal(hearingDay.get<Predicate>("courtCode"), courtCode),
            criteriaBuilder.equal(hearingOutcome.get<Predicate>("state"), hearingOutcomeSearchRequest.state.name)
        )

        hearingOutcomeSearchRequest.outcomeType?.let { outcomeTypes ->
            if(outcomeTypes.isNotEmpty()) {
                restrictions.add(hearingOutcome.get<Predicate>("outcomeType").`in`(hearingOutcomeSearchRequest.outcomeType.map { it.name }))
            }
        }

        criteriaQuery.where(*restrictions.toTypedArray())

        processSorting(hearingOutcomeSearchRequest, hearingDay, criteriaQuery, criteriaBuilder)
        return entityManager.createQuery(criteriaQuery)
            .resultList
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