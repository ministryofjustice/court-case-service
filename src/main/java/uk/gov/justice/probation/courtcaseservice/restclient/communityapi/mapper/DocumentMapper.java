package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionDocuments;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiGroupedDocumentsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderDocumentDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.document.ConvictionDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

@Component
public class DocumentMapper {

    public GroupedDocuments documentsFrom(CommunityApiGroupedDocumentsResponse documentsResponse) {
        return GroupedDocuments.builder()
            .documents(Optional.ofNullable(documentsResponse.getDocuments()).map(this::buildDocuments).orElse(Collections.emptyList()))
            .convictions(Optional.ofNullable(documentsResponse.getConvictions()).map(this::buildConvictionDocuments).orElse(Collections.emptyList()))
            .build();
    }

    private List<ConvictionDocuments> buildConvictionDocuments(List<CommunityApiConvictionDocuments> convictionDocuments) {
        return convictionDocuments.stream()
                .map(this::buildConvictionDocument)
                .collect(Collectors.toList());
    }

    private ConvictionDocuments buildConvictionDocument(CommunityApiConvictionDocuments convictionDocuments) {
        return ConvictionDocuments.builder()
            .convictionId(convictionDocuments.getConvictionId())
            .documents(Optional.ofNullable(convictionDocuments.getDocuments()).map(this::buildConvictionIdDocuments).orElse(Collections.emptyList()))
            .build();
    }

    private List<OffenderDocumentDetail> buildConvictionIdDocuments(List<CommunityApiOffenderDocumentDetail> documentDetails) {
        return documentDetails.stream()
            .map(this::buildOffenderDocumentDetail)
            .collect(Collectors.toList());
    }

    private List<OffenderDocumentDetail> buildDocuments(List<CommunityApiOffenderDocumentDetail> details) {
        return details.stream()
            .map(this::buildOffenderDocumentDetail)
            .collect(Collectors.toList());
    }

    private OffenderDocumentDetail buildOffenderDocumentDetail(CommunityApiOffenderDocumentDetail detail) {
        return OffenderDocumentDetail.builder()
            .documentId(detail.getId())
            .documentName(detail.getDocumentName())
            .author(detail.getAuthor())
            .type(DocumentType.valueOf(detail.getType().getCode()))
            .extendedDescription(detail.getExtendedDescription())
            .createdAt(detail.getCreatedAt())
            .reportDocumentDates(detail.getReportDocumentDates())
            .subType(detail.getSubType())
            .parentPrimaryKeyId(detail.getParentPrimaryKeyId())
            .build();
    }

}
