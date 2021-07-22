package uk.gov.justice.probation.courtcaseservice.restclient.prisonapi;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class PrisonApiSentencesResponse {
    private final PrisonApiSentenceDetail sentenceDetail;
}
