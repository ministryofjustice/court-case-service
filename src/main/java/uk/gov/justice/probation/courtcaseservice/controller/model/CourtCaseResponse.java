package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourtCaseResponse {
    private LocalDateTime lastUpdated;
    private String caseId;
    private String caseNo;
    private String courtCode;
    private String courtRoom;
    private LocalDateTime sessionStartTime;
    private String probationStatus;
    private LocalDate previouslyKnownTerminationDate;
    private Boolean suspendedSentenceOrder;
    private Boolean breach;
    private String data;
    private List<OffenceResponse> offences;
    private String defendantName;
    private AddressPropertiesEntity defendantAddress;
}
