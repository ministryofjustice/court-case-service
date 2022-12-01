package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

@Repository
public interface CaseSearchRepository extends PagingAndSortingRepository<HearingEntity, Long> {

    Page<HearingEntity> findAll(Specification<HearingEntity> searchSpecification, Pageable pageable);

}
