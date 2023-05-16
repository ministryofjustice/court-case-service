package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.controller.Constants;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenderMatchDetail {

    private final String title;
    private final String forename;
    private final List<String> middleNames;
    private final String surname;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_FORMAT)
    private final LocalDate dateOfBirth;
    private final Address address;
    private final MatchIdentifiers matchIdentifiers;
    private final DefendantProbationStatus probationStatus;
    @JsonProperty("mostRecentEvent")
    private final Event event;
    private final Double matchProbability;

    @JsonProperty
    public String getProbationStatusActual() {
        return Optional.ofNullable(probationStatus)
            .map(DefendantProbationStatus::name)
            .orElse(null);
    }
}
