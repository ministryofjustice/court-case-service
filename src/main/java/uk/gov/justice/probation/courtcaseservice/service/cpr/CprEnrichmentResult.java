package uk.gov.justice.probation.courtcaseservice.service.cpr;

public record CprEnrichmentResult(
    CprRefreshTarget target,
    CprEnrichmentStatus status,
    String reason
) {
    public static CprEnrichmentResult success(
        CprRefreshTarget target
    ) {
        return new CprEnrichmentResult(
            target,
            CprEnrichmentStatus.SUCCESS,
            null
        );
    }

    public static CprEnrichmentResult noLookupIdentifier(
        CprRefreshTarget target
    ) {
        return new CprEnrichmentResult(
            target,
            CprEnrichmentStatus.NO_LOOKUP_IDENTIFIER,
            "No defendantId or cId supplied"
        );
    }

    public static CprEnrichmentResult noCprRecord(
        CprRefreshTarget target
    ) {
        return new CprEnrichmentResult(
            target,
            CprEnrichmentStatus.NO_CPR_RECORD,
            "No CPR record found"
        );
    }

    public static CprEnrichmentResult failure(
        CprRefreshTarget target,
        Exception exception
    ) {
        return new CprEnrichmentResult(
            target,
            CprEnrichmentStatus.FAILED,
            exception.getMessage()
        );
    }
}