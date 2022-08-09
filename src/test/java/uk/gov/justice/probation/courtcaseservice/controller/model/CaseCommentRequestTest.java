package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;

import static org.assertj.core.api.Assertions.assertThat;

class CaseCommentRequestTest {

    @Test
    void asEntity() {
        var caseCommentRequest = CaseCommentRequest.builder()
            .comment("awaiting PSR")
            .caseId("test-cae-id")
            .build();

        assertThat(caseCommentRequest.asEntity())
            .isEqualTo(CaseCommentEntity.builder()
                .comment(caseCommentRequest.getComment())
                .caseId(caseCommentRequest.getCaseId())
                .build());
    }
}