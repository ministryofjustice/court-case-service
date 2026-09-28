package uk.gov.justice.probation.courtcaseservice.service.cpr;

import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

import java.util.List;

public interface CprEnrichmentService {

    List<CprEnrichmentOutcome> enrichHearingDefendants(
        HearingEntity hearing
    );

    CprEnrichmentResult enrich(
        DefendantEntity defendant,
        CprRefreshTarget target
    );

    List<CprEnrichmentResult> enrichAll(
        List<DefendantEntity> defendants,
        CprRefreshSource source
    );
}