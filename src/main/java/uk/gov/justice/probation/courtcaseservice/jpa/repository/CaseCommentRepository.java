package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;

public interface CaseCommentRepository extends CrudRepository<CaseCommentEntity, Long> {
}
