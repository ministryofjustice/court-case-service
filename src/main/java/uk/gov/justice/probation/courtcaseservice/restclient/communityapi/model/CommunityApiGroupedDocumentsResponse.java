package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityApiGroupedDocumentsResponse {

    private List<CommunityApiOffenderDocumentDetail> documents;
    private List<CommunityApiConvictionDocuments> convictions;

}
