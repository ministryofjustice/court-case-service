package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseCommentResponse {
    private final Long commentId;
    private final String caseId;
    private final String comment;
    private final String defendantId;
    private final LocalDateTime created;
    private final String author;
    private final String createdByUuid;
    private final boolean draft;
    private final boolean legacy;

    public static CaseCommentResponse of(CaseCommentEntity caseCommentEntity) {
        return CaseCommentResponse.builder()
                .comment(caseCommentEntity.getComment())
                .commentId(caseCommentEntity.getId())
                .caseId(caseCommentEntity.getCaseId())
                .defendantId(caseCommentEntity.getDefendantId())
                .created(caseCommentEntity.getCreated())
                .author(caseCommentEntity.getAuthor())
                .createdByUuid(caseCommentEntity.getCreatedByUuid())
                .draft(caseCommentEntity.isDraft())
                .legacy(caseCommentEntity.isLegacy())
                .build();
    }
}
