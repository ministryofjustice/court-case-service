package uk.gov.justice.probation.courtcaseservice.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus;

import java.time.LocalDate;

@Schema(description = "Probation Status Detail")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProbationStatusDetail {
    public static final ProbationStatusDetail NO_RECORD_STATUS = ProbationStatusDetail.builder().status(DefendantProbationStatus.UNCONFIRMED_NO_RECORD.name()).build();
    private final String status;
    private final LocalDate previouslyKnownTerminationDate;
    private final Boolean inBreach;
    private final boolean preSentenceActivity;
    private final Boolean awaitingPsr;
}
