package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springdoc.core.converters.models.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseSearchFilter;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

@Repository
public interface CaseSearchRepository extends PagingAndSortingRepository<HearingEntity, Long> {

    Page<HearingEntity> searchCases(CaseSearchFilter caseSearchFilter, Pageable pageable);
}
