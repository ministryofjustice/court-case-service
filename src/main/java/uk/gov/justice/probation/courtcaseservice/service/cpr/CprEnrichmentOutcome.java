package uk.gov.justice.probation.courtcaseservice.service.cpr;

import uk.gov.justice.probation.courtcaseservice.restclient.cpr.CprDefendant;

public record CprEnrichmentOutcome(
    CprRefreshTarget target,
    CprEnrichmentResult result,
    CprDefendant cprDefendant
) {
    public boolean hasCprRecord() {
        return cprDefendant != null;
    }

    public boolean hasGroupedMatches() {
        return hasCprRecord()
            && !cprDefendant.getCrns().isEmpty();
    }
}