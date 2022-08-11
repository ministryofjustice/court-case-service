package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;

import java.util.List;

public interface CaseCommentsRepository extends CrudRepository<CaseCommentEntity, Long> {
    List<CaseCommentEntity> findAllByCaseIdAndDeletedFalse(String caseId);
}
