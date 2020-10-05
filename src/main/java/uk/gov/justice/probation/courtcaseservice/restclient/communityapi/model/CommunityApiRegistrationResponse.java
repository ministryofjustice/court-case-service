package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiRegistrationResponse {

    private final LocalDate startDate;
    private final String notes;
    private final boolean active;
    private final LocalDate endDate;
    private final LocalDate nextReviewDate;
    private final KeyValue type;

}
