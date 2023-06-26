package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.springframework.stereotype.Repository
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingDay
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcome
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeSearchRequest
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import javax.persistence.EntityManager
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

        val hearingDay = root.join<HearingEntity, HearingDay>("hearingDays")
        val hearingOutcome = root.join<HearingEntity, HearingOutcomeEntity>("hearingOutcome")

        val restrictions = mutableListOf<Predicate>(
            criteriaBuilder.equal(hearingDay.get<Predicate>("courtCode"), courtCode),
            criteriaBuilder.equal(hearingOutcome.get<Predicate>("state"), hearingOutcomeSearchRequest.state.name)
        )

        if(hearingOutcomeSearchRequest.outcomeType.isNotEmpty()) {
            restrictions.add(hearingOutcome.get<Predicate>("outcomeType").`in`(hearingOutcomeSearchRequest.outcomeType.map { it.name }))
        }

        criteriaQuery.where(*restrictions.toTypedArray())

        return entityManager.createQuery(criteriaQuery)
            .resultList
    }
}