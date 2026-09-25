package uk.gov.justice.probation.courtcaseservice.service.cpr;

import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.restclient.cpr.CprDefendant;

public interface GroupedOffenderMatchUpdateService {

    void update(
            HearingEntity savedHearing,
            String defendantId,
            CprDefendant cprDefendant
    );
}