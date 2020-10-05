package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
public class CommunityApiOffenderManager {
    private final CommunityApiStaff staff;
    private final CommunityApiStaff trustOfficer;
    private final CommunityApiTeam team;
    private final KeyValue probationArea;
    private final LocalDate fromDate;

    private final Boolean active;
    private final Boolean softDeleted;
}
