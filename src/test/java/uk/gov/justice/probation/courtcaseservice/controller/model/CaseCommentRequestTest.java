package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;

import static org.assertj.core.api.Assertions.assertThat;

class CaseCommentRequestTest {

    @Test
    void asEntity() {
        var testComment = "awaiting PSR";
        String testCaseId = "test-case-id";
        var caseCommentRequest = CaseCommentRequest.builder()
            .comment(testComment)
            .caseId(testCaseId)
            .build();

        assertThat(caseCommentRequest.asEntity())
            .isEqualTo(CaseCommentEntity.builder()
                .comment(testComment)
                .caseId(testCaseId)
                .build());
    }
}