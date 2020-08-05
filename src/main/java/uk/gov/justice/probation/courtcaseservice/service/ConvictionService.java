package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CurrentOrderHeaderResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.SentenceResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.CustodialStatusNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ConvictionService {

    private final ConvictionRestClient restClient;

    @Autowired
    public ConvictionService(final ConvictionRestClient client) {
        this.restClient = client;
    }

    public SentenceResponse getConvictionOnly(final String crn, final Long convictionId) {
        return restClient.getConviction(crn, convictionId)
                .blockOptional()
                .map(conviction -> combineAttendancesAndUnpaidwork(Collections.emptyList(), conviction))
                .orElseThrow(() -> new OffenderNotFoundException(crn));
    }

    public SentenceResponse getSentence(final String crn, final Long convictionId, final Long sentenceId) {
        Mono<Tuple3<List<AttendanceResponse>, Conviction, CurrentOrderHeaderResponse>> sentenceMono = Mono.zip(
                restClient.getAttendances(crn, convictionId),
                restClient.getConviction(crn, convictionId),
                restClient.getCurrentOrderHeader(crn, convictionId, sentenceId)
        );

        var tuple3 = sentenceMono.blockOptional().orElseThrow(() -> new OffenderNotFoundException(crn));

        return combineOffenderAndConvictions(tuple3.getT1(), tuple3.getT2(), tuple3.getT3());
    }

    public CurrentOrderHeaderResponse getCurrentOrderHeader(final String crn, final Long convictionId, final Long sentenceId) {
        return restClient.getCurrentOrderHeader(crn, convictionId, sentenceId)
                .blockOptional()
                .orElseThrow(() -> new CustodialStatusNotFoundException(crn));
    }

    private SentenceResponse combineOffenderAndConvictions(List<AttendanceResponse> attendanceResponses, Conviction conviction, CurrentOrderHeaderResponse currentOrderHeaderResponse) {
        return SentenceResponse.builder()
                .attendances(attendanceResponses)
                .unpaidWork(Optional.ofNullable(conviction.getSentence()).map(Sentence::getUnpaidWork).orElse(null))
                .currentOrderHeaderDetail(currentOrderHeaderResponse)
                .build();
    }

    private SentenceResponse combineAttendancesAndUnpaidwork(List<AttendanceResponse> attendanceResponses, Conviction conviction) {
        return SentenceResponse.builder()
                .attendances(attendanceResponses)
                .unpaidWork(Optional.ofNullable(conviction.getSentence()).map(Sentence::getUnpaidWork).orElse(null))
                .build();
    }


}
