package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;



@Repository
public interface CourtRepository extends JpaRepository<CourtEntity, Long> {
    @Cacheable("Court")
    Optional<CourtEntity> findByCourtCode(String courtCode);
}
