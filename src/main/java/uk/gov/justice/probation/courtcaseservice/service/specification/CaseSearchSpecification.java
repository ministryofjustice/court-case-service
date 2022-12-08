package uk.gov.justice.probation.courtcaseservice.service.specification;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseSearchFilter;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;


public class CaseSearchSpecification {

    public static Specification<HearingEntity> getCaseSearchSpecification(CaseSearchFilter caseSearchFilter) {
        return buildMandatorySpecification(caseSearchFilter.getCourtCode(), caseSearchFilter.getDate());
    }

    private static Specification<HearingEntity> buildMandatorySpecification(String courtCode, LocalDate hearingDay) {
        return ((root, query, criteriaBuilder) -> {

            ListJoin<HearingEntity, HearingDayEntity> hearingDayEntityHearingEntityJoin = root.joinList("hearingDays");

            Predicate courtCodePredicate = criteriaBuilder.equal(hearingDayEntityHearingEntityJoin.get("courtCode"), courtCode);
            Predicate hearingDayPredicate = criteriaBuilder.equal(hearingDayEntityHearingEntityJoin.get("day"), hearingDay);

            return criteriaBuilder.and(courtCodePredicate, hearingDayPredicate);

        });
    }
}
