package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CaseCommentResponseTest {

    @Test
    void shouldMapCaseCommentEntityToModel() {
        final var caseCommentEntity = CaseCommentEntity.builder()
            .comment("PSR review")
            .commentId("test-uuid")
            .caseId("case-id-1")
            .created(LocalDateTime.now())
            .createdBy("test-user")
            .build();

        assertThat(CaseCommentResponse.of(caseCommentEntity)).isEqualTo(
            CaseCommentResponse.builder()
                .comment(caseCommentEntity.getComment())
                .commentId(caseCommentEntity.getCommentId())
                .caseId(caseCommentEntity.getCaseId())
                .createdBy(caseCommentEntity.getCreatedBy())
                .created(caseCommentEntity.getCreated())
                .build()
        );
    }
}