package uk.gov.justice.probation.courtcaseservice.service.mapper;

import uk.gov.justice.probation.courtcaseservice.restclient.prisonapi.PrisonApiSentencesResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Custody;

import java.util.Optional;

public class CustodyMapper {
    public static Custody custodyFrom(PrisonApiSentencesResponse prisonApiSentencesResponse) {

        return Optional.ofNullable(prisonApiSentencesResponse.getSentenceDetail())
                .map(sentenceDetail -> Custody.builder()
                        .homeDetentionCurfewActualDate(sentenceDetail.getHomeDetentionCurfewActualDate())
                        .homeDetentionCurfewEndDate(sentenceDetail.getHomeDetentionCurfewEndDate())
                        .licenceExpiryDate(sentenceDetail.getLicenceExpiryDate())
                        .releaseDate(sentenceDetail.getReleaseDate())
                        .topupSupervisionStartDate(sentenceDetail.getTopupSupervisionStartDate())
                        .topupSupervisionExpiryDate(sentenceDetail.getTopupSupervisionExpiryDate())
                        .build())
                .orElse(Custody.builder().build());
    }
}
