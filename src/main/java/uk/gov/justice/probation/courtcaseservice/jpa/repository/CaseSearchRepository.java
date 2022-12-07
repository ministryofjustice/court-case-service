package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

import java.util.List;

@Repository
public interface CaseSearchRepository extends JpaRepository<HearingEntity, Long> {

    List<HearingEntity> findAll(Specification<HearingEntity> searchSpecification);

}
