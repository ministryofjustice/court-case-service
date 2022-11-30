package uk.gov.justice.probation.courtcaseservice.jpa.specification;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class CourtCaseSpecification implements Specification<CourtCaseEntity> {

    @Override
    public Predicate toPredicate(Root<CourtCaseEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return null;
    }
}
