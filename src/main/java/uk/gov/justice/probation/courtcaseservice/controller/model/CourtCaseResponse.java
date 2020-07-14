package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;

@ApiModel(description = "Case Information")
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtCaseResponse {
    private final LocalDateTime lastUpdated;
    private final String caseId;
    private final String caseNo;
    private final String crn;
    private final String pnc;
    private final String cro;
    private final String listNo;
    private final String courtCode;
    private final String courtRoom;
    private final LocalDateTime sessionStartTime;
    private final CourtSession session;
    private final String probationStatus;
    private final LocalDate previouslyKnownTerminationDate;
    private final Boolean suspendedSentenceOrder;
    private final Boolean breach;
    private final List<OffenceResponse> offences;
    private final String defendantName;
    private final AddressPropertiesEntity defendantAddress;
    private final LocalDate defendantDob;
    private final String defendantSex;
    private final String nationality1;
    private final String nationality2;
    private final boolean createdToday;
    private final boolean removed;
}


