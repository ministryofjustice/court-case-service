package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CaseDefendantEntityTest {

    @Test
    void givenDocumentIdAndFileName_shouldAddCaseDocument() {
        var caseDefendant = CaseDefendantEntity.builder().build();
        String fileName = "file-name-one";
        String documentUuid = "document-id-one";
        caseDefendant.createDocument(documentUuid, fileName);

        CaseDefendantDocumentEntity actual = caseDefendant.getDocuments().get(0);
        Assertions.assertThat(actual.getDocumentId()).isEqualTo(documentUuid);
        Assertions.assertThat(actual.getDocumentName()).isEqualTo(fileName);
    }

    @Test
    void givenDocumentId_shouldFindCaseDocument() {
        var caseDefendant = CaseDefendantEntity.builder().build();
        String fileName = "file-name-one";
        String documentUuid = "document-id-one";
        caseDefendant.createDocument(documentUuid, fileName);
        caseDefendant.createDocument("document-two", "filename-tow");

        CaseDefendantDocumentEntity actual = caseDefendant.getCaseDefendantDocument(documentUuid);
        Assertions.assertThat(actual.getDocumentId()).isEqualTo(documentUuid);
        Assertions.assertThat(actual.getDocumentName()).isEqualTo(fileName);
    }

}