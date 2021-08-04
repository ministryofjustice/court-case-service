package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class CommunityApiSentenceStatusResponse {
    private final Long sentenceId;
    private final String sentenceDescription;
    private final KeyValue custodialType;
    private final String mainOffenceDescription;
    private final LocalDate sentenceDate;
    private final LocalDate actualReleaseDate;
    private final LocalDate licenceExpiryDate;
    private final LocalDate pssEndDate;
    private final Integer length;
    private final String lengthUnits;
}



