package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;

import java.util.List;
import java.util.Optional;

public interface CaseCommentsRepository extends CrudRepository<CaseCommentEntity, Long> {
    List<CaseCommentEntity> findByCaseIdAndDefendantIdAndDeletedFalse(String caseId, String defendantId);

    Optional<CaseCommentEntity> findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(String caseId, String defendantId, String userUuid);
    Optional<CaseCommentEntity> findByIdAndCaseIdAndDefendantIdAndCreatedByUuid(Long commentId, String caseId, String defendantId, String userUuid);
}
