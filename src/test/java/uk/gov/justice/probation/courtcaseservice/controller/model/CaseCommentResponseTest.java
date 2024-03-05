package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CaseCommentResponseTest {

    @Test
    void shouldMapCaseCommentEntityToModel() {
        var now = LocalDateTime.now();
        final var caseCommentEntity = CaseCommentEntity.builder()
            .comment("PSR review")
            .id(1234L)
            .caseId("case-id-1")
            .defendantId("defendant-id")
            .created(now)
            .author("test-user")
            .draft(true)
            .build();

        assertThat(CaseCommentResponse.of(caseCommentEntity)).isEqualTo(
            CaseCommentResponse.builder()
                .comment("PSR review")
                .commentId(1234L)
                .caseId("case-id-1")
                .defendantId("defendant-id")
                .author("test-user")
                .created(now)
                .draft(true)
                .build()
        );
    }
}