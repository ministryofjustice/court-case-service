package uk.gov.justice.probation.courtcaseservice.service.cpr;

import java.util.Optional;

public interface OffenderSearchEnrichmentService {

    Optional<OffenderSearchResult> findSingleOffender(String crn);
}