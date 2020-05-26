package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.document.ReportDocumentDates;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityApiOffenderDocumentDetail {

    private String id;
    private String documentName;
    private String author;
    private KeyValue type;
    private String extendedDescription;
    private LocalDateTime lastModifiedAt;
    private LocalDateTime createdAt;
    private Long parentPrimaryKeyId;

    private KeyValue subType;
    private ReportDocumentDates reportDocumentDates;
}

