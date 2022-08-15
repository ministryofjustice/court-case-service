package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;

import static org.assertj.core.api.Assertions.assertThat;

class CaseCommentRequestTest {

    @Test
    void asEntity() {
        var testComment = "awaiting PSR";
        var testCaseId = "test-case-id";
        var author = "Adam Sandler";
        var userUuid = "test-user-uuid";
        var caseCommentRequest = CaseCommentRequest.builder()
            .comment(testComment)
            .caseId(testCaseId)
            .author(author)
            .userUuid(userUuid)
            .build();

        assertThat(caseCommentRequest.asEntity())
            .isEqualTo(CaseCommentEntity.builder()
                .comment(testComment)
                .caseId(testCaseId)
                .createdByUuid(userUuid)
                .author(author)
                .build());
    }
}