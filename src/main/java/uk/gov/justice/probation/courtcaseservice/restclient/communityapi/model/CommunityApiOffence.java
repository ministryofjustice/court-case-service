package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiOffence {
    private final CommunityApiOffenceDetail detail;
    private final LocalDate offenceDate;
    private final boolean mainOffence;
}
