package uk.gov.justice.probation.courtcaseservice.service.mapper;

import uk.gov.justice.probation.courtcaseservice.restclient.prisonapi.PrisonApiSentencesResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Custody;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

public class CustodyMapper {
    public static Custody custodyFrom(PrisonApiSentencesResponse prisonApiSentencesResponse) {

        return Optional.ofNullable(prisonApiSentencesResponse.getSentenceDetail())
                .map(sentenceDetail -> Custody.builder()
                        .homeDetentionCurfewActualDate(localDateOf(sentenceDetail.getHomeDetentionCurfewActualDate()))
                        .homeDetentionCurfewEndDate(localDateOf(sentenceDetail.getHomeDetentionCurfewEndDate()))
                        .licenceExpiryDate(localDateOf(sentenceDetail.getLicenceExpiryDate()))
                        .releaseDate(localDateOf(sentenceDetail.getReleaseDate()))
                        .topupSupervisionStartDate(localDateOf(sentenceDetail.getTopupSupervisionStartDate()))
                        .topupSupervisionExpiryDate(localDateOf(sentenceDetail.getTopupSupervisionExpiryDate()))
                        .build())
                .orElse(Custody.builder().build());
    }

    private static LocalDate localDateOf(ZonedDateTime homeDetentionCurfewDateActual) {
        return Optional.ofNullable(homeDetentionCurfewDateActual).map(ZonedDateTime::toLocalDate).orElse(null);
    }
}
