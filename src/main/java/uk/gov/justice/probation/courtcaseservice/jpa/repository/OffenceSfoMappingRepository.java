package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceSfoMappingEntity;

import java.util.Collection;
import java.util.List;

public interface OffenceSfoMappingRepository extends JpaRepository<OffenceSfoMappingEntity, Long> {

    List<OffenceSfoMappingEntity> findByOffenceCodeIn(Collection<String> offenceCodes);
}

