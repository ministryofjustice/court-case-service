package uk.gov.justice.probation.courtcaseservice.controller.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;

@ApiModel(description = "Case Information")
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtCaseResponse {
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
    private final Boolean preSentenceActivity;
    private final List<OffenceResponse> offences;
    private final String defendantName;
    private final NamePropertiesEntity name;
    private final AddressPropertiesEntity defendantAddress;
    private final LocalDate defendantDob;
    private final String defendantSex;
    private final DefendantType defendantType;
    private final String nationality1;
    private final String nationality2;
    private final boolean createdToday;
    private final boolean removed;
    private final Long numberOfPossibleMatches;
}


