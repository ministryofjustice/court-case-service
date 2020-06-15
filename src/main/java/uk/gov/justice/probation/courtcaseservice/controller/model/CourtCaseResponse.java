package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@ApiModel(description = "Case Information")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtCaseResponse {
    private LocalDateTime lastUpdated;
    private String caseId;
    private String caseNo;
    private String crn;
    private String pnc;
    private String listNo;
    private String courtCode;
    private String courtRoom;
    private LocalDateTime sessionStartTime;
    private CourtSession session;
    private String probationStatus;
    private LocalDate previouslyKnownTerminationDate;
    private Boolean suspendedSentenceOrder;
    private Boolean breach;
    private List<OffenceResponse> offences;
    private String defendantName;
    private AddressPropertiesEntity defendantAddress;
    private LocalDate defendantDob;
    private String defendantSex;
    private String nationality1;
    private String nationality2;
}


