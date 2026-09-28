package uk.gov.justice.probation.courtcaseservice.service.cpr;

public record CprRefreshTarget(
    String defendantId,
    String cId,
    CprRefreshSource source
) {
    public boolean hasLookupIdentifier() {
        return defendantId != null || cId != null;
    }
}