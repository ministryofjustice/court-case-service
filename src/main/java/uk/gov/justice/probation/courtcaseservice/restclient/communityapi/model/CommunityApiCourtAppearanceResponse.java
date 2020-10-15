package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;


import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CommunityApiCourtAppearanceResponse {

    private final Long courtAppearanceId;
    private final LocalDateTime appearanceDate;
    private final String courtCode;
    private final String courtName;
    private final KeyValue appearanceType;
    private final String crn;
}
