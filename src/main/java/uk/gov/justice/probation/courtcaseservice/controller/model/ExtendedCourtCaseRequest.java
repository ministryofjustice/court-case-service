package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
public class ExtendedCourtCaseRequest {
    private final String caseNo;
    private final String courtCode;
    private final String courtRoom;
    private final List<HearingDay> hearingDays;
    private final List<OffenceRequest> offences;
    private final List<Defendant> defendants;

}
