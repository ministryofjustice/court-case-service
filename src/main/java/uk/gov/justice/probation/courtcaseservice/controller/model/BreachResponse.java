package uk.gov.justice.probation.courtcaseservice.controller.model;

import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BreachResponse {
    private final Long breachId;
    private final LocalDate incidentDate;
    private final LocalDate started;
    private final LocalDate statusDate;
    private final String provider;
    private final String team;
    private final String officer;
    private final String status;
    private final String order;
    private final String sentencingCourtName;
    private final String notes;

    private final List<OffenderDocumentDetail> documents;
}
