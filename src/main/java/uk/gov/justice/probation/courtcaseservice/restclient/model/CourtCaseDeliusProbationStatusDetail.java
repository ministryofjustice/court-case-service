package uk.gov.justice.probation.courtcaseservice.restclient.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtCaseDeliusProbationStatusDetail {

    private final ProbationStatus status;
    private final LocalDate terminationDate;
    private final Boolean inBreach;
    private final boolean preSentenceActivity;
    private final Boolean awaitingPsr;
}

enum ProbationStatus {
    NO_RECORD,
    NOT_SENTENCED,
    PREVIOUSLY_KNOWN,
    CURRENT
}
