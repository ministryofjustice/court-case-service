package uk.gov.justice.probation.courtcaseservice.service;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType.COURT_REPORT_DOCUMENT;
import static uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType.INSTITUTION_REPORT_DOCUMENT;

import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

class DocumentTypeFilterTest {

    private final OffenderDocumentDetail psrDocument = OffenderDocumentDetail.builder()
        .type(COURT_REPORT_DOCUMENT)
        .subType(new KeyValue("CODE", "Description"))
        .build();

    private final OffenderDocumentDetail institutionDocument = OffenderDocumentDetail.builder()
        .type(INSTITUTION_REPORT_DOCUMENT)
        .subType(new KeyValue("CODE", "Description"))
        .build();

    @DisplayName("Ensures that building the filter with nulls means the document is not filtered")
    @Test
    void givenNullInputs_WhenFilter_ThenIsTrue() {
        DocumentTypeFilter filter = new DocumentTypeFilter(null, null);

        assertTrue(filter.test(institutionDocument));
    }

    @DisplayName("Ensures that building the filter with empty means the document is not filtered")
    @Test
    void givenEmptyInputs_WhenFilter_ThenIsTrue() {
        DocumentTypeFilter filter = new DocumentTypeFilter(Collections.emptyList(), Collections.emptyList());

        assertTrue(filter.test(institutionDocument));
    }

    @DisplayName("Ensures that a filter matching the document types is not filtered")
    @Test
    void givenMatchingDocument_WhenFilter_ThenIsTrue() {
        DocumentTypeFilter filter = new DocumentTypeFilter(singletonList(COURT_REPORT_DOCUMENT), singletonList("CODE"));

        assertTrue(filter.test(psrDocument));
    }

    @DisplayName("Ensures that a filter not matching the document types is filtered")
    @Test
    void givenNonMatchingDocumentOnType_WhenFilter_ThenIsFalse() {
        DocumentTypeFilter filter = new DocumentTypeFilter(singletonList(INSTITUTION_REPORT_DOCUMENT), singletonList("CODE"));

        assertFalse(filter.test(psrDocument));
    }

    @DisplayName("Ensures that a filter not matching the document types is filtered")
    @Test
    void givenMatchingDocumentOnTypeButNoCode_WhenFilter_ThenIsFalse() {

        OffenderDocumentDetail nullCodeDocument = OffenderDocumentDetail.builder()
            .type(COURT_REPORT_DOCUMENT)
            .build();

        DocumentTypeFilter filter = new DocumentTypeFilter(singletonList(COURT_REPORT_DOCUMENT), singletonList("CODE"));

        assertFalse(filter.test(nullCodeDocument));
    }

}
