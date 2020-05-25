package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BreachResponse {
    private final Long breachId;
    private final LocalDate incidentDate;
    private final LocalDate started;
    private final String provider;
    private final String team;
    private final String officer;
    private final String status;
    private final String order;

    private final List<OffenderDocumentDetail> documents;
}
