package uk.gov.justice.probation.courtcaseservice.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

@Component
public class DocumentTypeFilter implements Predicate<OffenderDocumentDetail> {

    private final Set<DocumentType> documentTypes;

    private final Set<String> subTypeCodes;

    public DocumentTypeFilter(@Value("#{'${community-api.probation-record-filter.document.types}'.split(',')}") List<DocumentType> documentTypes,
                            @Value("#{'${community-api.probation-record-filter.document.subtype.codes}'.split(',')}") List<String> subTypeCodes) {
        this.documentTypes = documentTypes == null ? new HashSet<>() : new HashSet<>(documentTypes);
        this.subTypeCodes = subTypeCodes == null ? new HashSet<>() : new HashSet<>(subTypeCodes);
    }

    @Override
    public boolean test(final OffenderDocumentDetail documentDetail) {
        if (documentTypes.isEmpty() && subTypeCodes.isEmpty()) {
            return true;
        }

        final Optional<String> subTypeKeyValue = Optional.ofNullable(documentDetail.getSubType()).map(KeyValue::getCode);

        return subTypeKeyValue.isPresent()
            && documentTypes.contains(documentDetail.getType())
            && subTypeCodes.contains(subTypeKeyValue.get());
    }
}
