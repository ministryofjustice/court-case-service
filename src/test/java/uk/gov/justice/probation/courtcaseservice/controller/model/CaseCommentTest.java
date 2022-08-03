package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CaseCommentTest {

    @Test
    void shouldMapCaseCommentEntityToModel() {
        final var caseCommentEntity = CaseCommentEntity.builder()
            .comment("PSR review")
            .commentId("test-uuid")
            .created(LocalDateTime.now())
            .createdBy("test-user")
            .build();
        assertThat(CaseComment.of(caseCommentEntity)).isEqualTo(
            CaseComment.builder()
                .comment(caseCommentEntity.getComment())
                .commentId(caseCommentEntity.getCommentId())
                .createdBy(caseCommentEntity.getCreatedBy())
                .created(caseCommentEntity.getCreated())
                .build()
        );
    }
}