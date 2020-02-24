package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;

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
    private String data;
    @JsonIgnore
    private List<OffenceEntity> offences;
}
