package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefendantOffender {
    private final String probationStatus;
    @NotBlank
    private final String crn;
    private final LocalDate previouslyKnownTerminationDate;
    private final Boolean suspendedSentenceOrder;
    private final Boolean breach;
    private final Boolean preSentenceActivity;
    private final Boolean awaitingPsr;

    public OffenderEntity asEntity() {
        return OffenderEntity.builder()
                .crn(crn)
                .probationStatus(OffenderProbationStatus.of(probationStatus))
                .previouslyKnownTerminationDate(previouslyKnownTerminationDate)
                .awaitingPsr(awaitingPsr)
                .breach(Optional.ofNullable(breach).orElse(false))
                .preSentenceActivity(Optional.ofNullable(preSentenceActivity).orElse(false))
                .suspendedSentenceOrder(Optional.ofNullable(suspendedSentenceOrder).orElse(false))
                .build();
    }

    public static DefendantOffender of(final OffenderEntity offenderEntity) {

        return DefendantOffender.builder()
                .crn(offenderEntity.getCrn())
                .probationStatus(Optional.ofNullable(offenderEntity.getProbationStatus()).map(OffenderProbationStatus::getName).orElse(null))
                .previouslyKnownTerminationDate(offenderEntity.getPreviouslyKnownTerminationDate())
                .awaitingPsr(offenderEntity.getAwaitingPsr())
                .breach(offenderEntity.isBreach())
                .preSentenceActivity(offenderEntity.isPreSentenceActivity())
                .suspendedSentenceOrder(offenderEntity.isSuspendedSentenceOrder())
                .build();
    }

    public static DefendantOffender of(final DefendantEntity defendantEntity) {
        return DefendantOffender.builder().build(); // TODO
    }
}
