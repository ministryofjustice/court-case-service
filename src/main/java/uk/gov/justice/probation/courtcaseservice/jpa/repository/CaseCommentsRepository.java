package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;

import java.util.List;
import java.util.Optional;

public interface CaseCommentsRepository extends CrudRepository<CaseCommentEntity, Long> {
    List<CaseCommentEntity> findAllByCaseIdAndDeletedFalse(String caseId);

    Optional<CaseCommentEntity> findByCaseIdAndCreatedByUuidAndDraftIsTrue(String caseId, String userUuid);
}
