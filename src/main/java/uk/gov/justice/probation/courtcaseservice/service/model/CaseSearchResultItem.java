package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcome;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "SearchResultItem")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CaseSearchResultItem {

    private String defendantName;

    private String hearingId;

    private String defendantId;

    private String crn;

    private DefendantProbationStatus probationStatus;

    private List<String> offenceTitles;

    private LocalDate lastHearingDate;

    private LocalDate nextHearingDate;

    private String lastHearingCourt;

    private String nextHearingCourt;

    private final Boolean awaitingPsr;

    private final Boolean breach;

    private final HearingOutcome hearingOutcome;
}
