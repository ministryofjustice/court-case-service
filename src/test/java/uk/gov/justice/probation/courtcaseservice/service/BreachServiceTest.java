package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.DocumentRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.NsiRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiType;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.NsiNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.CourtAppearance;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class BreachServiceTest {
    public static final String CRN = "CRN";
    public static final long CONVICTION_ID = 12358073L;
    public static final long BREACH_ID = 1267523687L;
    private static final String SENTENCE_TYPE = "S";
    private static final String COURT_NAME = "Sheffield Crown Court";
    private static final String COURT_CODE = "B14LO00";

    private final CourtAppearance courtAppearance = buildCourtAppearance(LocalDateTime.now().minusMonths(1), "S", COURT_NAME);

    private final BreachResponse expectedBreachResponse = BreachResponse.builder()
                                                        .breachId(BREACH_ID)
                                                        .documents(Collections.emptyList())
                                                        .sentencingCourtName(COURT_NAME)
                                                        .build();

    @Mock
    private CommunityApiNsi nsi;
    @Mock
    private CommunityApiNsiType nsiType;
    @Mock
    private GroupedDocuments groupedDocuments;
    @Mock
    private Conviction conviction;
    @Mock
    private NsiRestClient nsiRestClient;
    @Mock
    private ConvictionRestClient convictionRestClient;
    @Mock
    private DocumentRestClient documentRestClient;
    @Mock
    private OffenderRestClientFactory offenderRestClientFactory;
    @Mock
    private OffenderRestClient offenderRestClient;

    private BreachService breachService;

    @BeforeEach
    void setUp() {
        when(offenderRestClientFactory.build()).thenReturn(offenderRestClient);
        breachService = new BreachService(nsiRestClient, convictionRestClient, documentRestClient, offenderRestClientFactory, Arrays.asList("BRE", "BRES"), "S");
    }

    @Test
    void whenGetBreachHasTypeBRE_thenReturnBreach() {
        when(nsiType.getCode()).thenReturn("BRE");
        mockForGetBreach();

        BreachResponse actualBreachResponse = breachService.getBreach(CRN, CONVICTION_ID, BREACH_ID);

        assertThat(actualBreachResponse).isEqualTo(expectedBreachResponse);
    }

    @Test
    void whenGetBreachHasTypeBRES_thenReturnBreach() {
        when(nsiType.getCode()).thenReturn("BRES");
        mockForGetBreach();

        BreachResponse actualBreachResponse = breachService.getBreach(CRN, CONVICTION_ID, BREACH_ID);

        assertThat(actualBreachResponse).isEqualTo(expectedBreachResponse);
    }

    @Test
    void whenNsiIsNotABreach_thenThrowNotFoundException() {
        when(nsiType.getCode()).thenReturn("NOTBRE");
        when(nsi.getNsiId()).thenReturn(BREACH_ID);
        mockForGetBreach();

        assertThatExceptionOfType(NsiNotFoundException.class)
                .isThrownBy(() -> breachService.getBreach(CRN, CONVICTION_ID, BREACH_ID))
                .withMessage("Breach with id '1267523687' does not exist");
    }

    @Test
    void givenMultipleAppearances_whenFindSentencingCourt_thenReturn() {
        LocalDateTime now = LocalDateTime.now();
        var sentenceAppearance1 = buildCourtAppearance(now.minusDays(1), SENTENCE_TYPE, COURT_NAME);
        var sentenceAppearance2 = buildCourtAppearance(now.minusDays(2), SENTENCE_TYPE, "Liverpool");
        var appearance1 = buildCourtAppearance(now, "M", "Newcastle");
        var appearance2 = buildCourtAppearance(now.plusDays(1), "M", "London");
        var appearance3 = buildCourtAppearance(now.minusDays(2), "M", "Newcastle");

        String courtName = breachService.findLatestSentencingCourt(List.of(appearance1, sentenceAppearance1, appearance2, sentenceAppearance2, appearance3));

        assertThat(courtName).isEqualTo(COURT_NAME);
    }

    @Test
    void givenNoSentenceAppearances_whenFindSentencingCourt_thenReturnNull() {
        LocalDateTime now = LocalDateTime.now();
        var appearance1 = buildCourtAppearance(now, "M", "Newcastle");
        var appearance2 = buildCourtAppearance(now.plusDays(1), "M", "London");
        var appearance3 = buildCourtAppearance(now.minusDays(2), "M", "Newcastle");

        String courtName = breachService.findLatestSentencingCourt(List.of(appearance1, appearance2, appearance3));

        assertThat(courtName).isNull();
    }

    private CourtAppearance buildCourtAppearance(LocalDateTime date, String type, String courtName) {
        return CourtAppearance.builder()
            .date(date)
            .type(new KeyValue(type, "CODE"))
            .courtCode(COURT_CODE)
            .courtName(courtName)
            .build();
    }

    private void mockForGetBreach() {
        CourtAppearance nonSentenceAppearance = buildCourtAppearance(LocalDateTime.now(), "M", "Newcastle");
        when(nsi.getType()).thenReturn(nsiType);
        when(nsi.getNsiId()).thenReturn(BREACH_ID);
        when(nsiRestClient.getNsiById(CRN, CONVICTION_ID, BREACH_ID)).thenReturn(Mono.just(nsi));
        when(convictionRestClient.getConviction(CRN, CONVICTION_ID)).thenReturn(Mono.just(conviction));
        when(documentRestClient.getDocumentsByCrn(CRN)).thenReturn(Mono.just(groupedDocuments));
        when(offenderRestClient.getOffenderCourtAppearances(CRN, CONVICTION_ID)).thenReturn(Mono.just(List.of(nonSentenceAppearance, courtAppearance)));
    }
}
