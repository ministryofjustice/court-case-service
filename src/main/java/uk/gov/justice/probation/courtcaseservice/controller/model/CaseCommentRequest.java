package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseCommentRequest {
    @NotBlank
    private final String caseId;
    @NotBlank
    private final String comment;
    @NotBlank
    private final String author;

    public CaseCommentEntity asEntity(String userUuid, String caseId, String defendantId) {
        return CaseCommentEntity.builder()
            .comment(comment)
            .caseId(caseId)
            .defendantId(defendantId)
            .createdByUuid(userUuid)
            .author(author)
            .build();
    }
}
