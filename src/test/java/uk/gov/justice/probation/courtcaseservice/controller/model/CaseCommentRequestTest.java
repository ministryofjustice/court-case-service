package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;

import static org.assertj.core.api.Assertions.assertThat;

class CaseCommentRequestTest {

    @Test
    void asEntity() {
        var testComment = "awaiting PSR";
        var testCaseId = "test-case-id";
        var testDefendantId = "test-defendant-id";
        var author = "Adam Sandler";
        var userUuid = "test-user-uuid";
        var caseCommentRequest = CaseCommentRequest.builder()
            .comment(testComment)
            .caseId(testCaseId)
            .author(author)
            .build();

        assertThat(caseCommentRequest.asEntity(userUuid, testCaseId, testDefendantId))
            .isEqualTo(CaseCommentEntity.builder()
                .comment(testComment)
                .caseId(testCaseId)
                .defendantId(testDefendantId)
                .createdByUuid(userUuid)
                .author(author)
                .build());
    }
}