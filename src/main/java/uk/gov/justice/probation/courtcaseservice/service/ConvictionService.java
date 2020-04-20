package uk.gov.justice.probation.courtcaseservice.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.ConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;

@Service
public class ConvictionService {

    private final ConvictionRestClient restClient;

    @Autowired
    public ConvictionService(final ConvictionRestClient client) {
        this.restClient = client;
    }

    public ConvictionResponse getConvictionNoAttendances(final String crn, final Long convictionId) {
        return restClient.getConviction(crn, convictionId)
            .blockOptional()
            .map(conviction -> combineOffenderAndConvictions(Collections.emptyList(), conviction))
            .orElseThrow(() -> new OffenderNotFoundException(crn));
    }

    public ConvictionResponse getConviction(final String crn, final Long convictionId) {

        return Mono.zip(restClient.getAttendances(crn, convictionId), restClient.getConviction(crn, convictionId), this::combineOffenderAndConvictions)
            .blockOptional()
            .orElseThrow(() -> new OffenderNotFoundException(crn));
    }

    private ConvictionResponse combineOffenderAndConvictions(List<AttendanceResponse> attendanceResponses, Conviction conviction) {
        return ConvictionResponse.builder()
            .attendances(attendanceResponses)
            .unpaidWork(Optional.ofNullable(conviction.getSentence()).map(Sentence::getUnpaidWork).orElse(null))
            .build();
    }

}
