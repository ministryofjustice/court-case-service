package uk.gov.justice.probation.courtcaseservice.service.specification;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseSearchFilter;
import uk.gov.justice.probation.courtcaseservice.controller.model.Defendant;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.List;


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

    private static Specification<HearingEntity> buildProbationStatusFilter(List<String> probationStatusList) {
        return ((root, query, criteriaBuilder) -> {
            ListJoin<HearingEntity, HearingDefendantEntity> hearingEntityHearingDefendantEntityListJoin = root.joinList("hearingDefendants");
            Join<HearingDefendantEntity, Defendant> hearingDefendantEntityDefendantJoin = hearingEntityHearingDefendantEntityListJoin.join("defendant");
            Join<DefendantEntity, OffenderEntity> defendantEntityOffenderEntityJoin = hearingDefendantEntityDefendantJoin.join("offender");

            return defendantEntityOffenderEntityJoin.get("probationStatus").in(probationStatusList);
        });
    }


}
