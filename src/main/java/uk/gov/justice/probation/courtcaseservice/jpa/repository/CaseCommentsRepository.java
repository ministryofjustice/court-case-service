package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CaseCommentsRepository extends CrudRepository<CaseCommentEntity, Long> {
    List<CaseCommentEntity> findByCaseIdAndDefendantIdAndDeletedFalse(String caseId, String defendantId);
    List<CaseCommentEntity> findByDefendantId(String defendantId);
    List<CaseCommentEntity> findByDefendantIdAndCreatedBetween(String defendantId, LocalDateTime fromDate, LocalDateTime toDate);
    List<CaseCommentEntity> findByDefendantIdAndCreatedAfter(String defendantId, LocalDateTime fromDate);
    List<CaseCommentEntity> findByDefendantIdAndCreatedBefore(String defendantId, LocalDateTime toDate);

    Optional<CaseCommentEntity> findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(String caseId, String defendantId, String userUuid);
    List<CaseCommentEntity> findAllByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrueOrderByCreatedDesc(String caseId, String defendantId, String userUuid);
    Optional<CaseCommentEntity> findByIdAndCaseIdAndDefendantIdAndCreatedByUuid(Long commentId, String caseId, String defendantId, String userUuid);
}
