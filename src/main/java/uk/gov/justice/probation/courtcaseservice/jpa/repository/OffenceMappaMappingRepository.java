package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceMappaMappingEntity;

import java.util.Collection;
import java.util.List;

public interface OffenceMappaMappingRepository extends JpaRepository<OffenceMappaMappingEntity, Long> {

    List<OffenceMappaMappingEntity> findByOffenceCodeIn(Collection<String> offenceCodes);
}
