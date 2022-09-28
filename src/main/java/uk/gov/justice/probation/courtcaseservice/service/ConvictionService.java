package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CustodyDetail;
import uk.gov.justice.probation.courtcaseservice.controller.model.SentenceLinks;
import uk.gov.justice.probation.courtcaseservice.controller.model.SentenceResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.CustodialStatusNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;
import uk.gov.justice.probation.courtcaseservice.service.model.SentenceStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ConvictionService {

    private final ConvictionRestClient convictionRestClient;
    private final OffenderRestClient offenderRestClient;
    private final String deliusContactListUrlTemplate;

    @Autowired
    public ConvictionService(final ConvictionRestClient client,
                             final OffenderRestClientFactory offenderRestClientFactory,
                             @Value("${delius.contact-list-url-template}") final String deliusContactListUrlTemplate) {
        this.convictionRestClient = client;
        this.offenderRestClient = offenderRestClientFactory.buildUserAwareOffenderRestClient();
        this.deliusContactListUrlTemplate = deliusContactListUrlTemplate;
    }

    public SentenceResponse getConvictionOnly(final String crn, final Long convictionId) {
        return convictionRestClient.getConviction(crn, convictionId)
                .blockOptional()
                .map(conviction -> combineAttendancesAndUnpaidwork(Collections.emptyList(), conviction))
                .orElseThrow(() -> new OffenderNotFoundException(crn));
    }

    public SentenceResponse getSentence(final String crn, final Long convictionId) {
        var sentenceMono = Mono.zip(
                convictionRestClient.getAttendances(crn, convictionId),
                convictionRestClient.getConviction(crn, convictionId),
                convictionRestClient.getSentenceStatus(crn, convictionId),
                offenderRestClient.getOffender(crn)
        );

        var tuple4 = sentenceMono.blockOptional().orElseThrow(() -> new OffenderNotFoundException(crn));

        return combineOffenderAndConvictions(tuple4.getT1(), tuple4.getT2(), tuple4.getT3(), tuple4.getT4());
    }

    public SentenceStatus getSentenceStatus(final String crn, final Long convictionId) {
        return convictionRestClient.getSentenceStatus(crn, convictionId)
                .blockOptional()
                .orElseThrow(() -> new CustodialStatusNotFoundException(crn));
    }

    private SentenceResponse combineOffenderAndConvictions(List<AttendanceResponse> attendanceResponses, Conviction conviction, SentenceStatus sentenceStatus, CommunityApiOffenderResponse offenderDetail) {        var builder = SentenceResponse.builder()
                .attendances(attendanceResponses)
                .unpaidWork(Optional.ofNullable(conviction.getSentence()).map(Sentence::getUnpaidWork).orElse(null))
                .links(SentenceLinks.builder()
                        .deliusContactList(String.format(deliusContactListUrlTemplate, offenderDetail.getOffenderId(), conviction.getConvictionId()))
                        .build())
                .sentenceId(sentenceStatus.getSentenceId())
                .sentenceDescription(sentenceStatus.getSentenceDescription())
                .mainOffenceDescription(sentenceStatus.getMainOffenceDescription())
                .sentenceDate(sentenceStatus.getSentenceDate())
                .actualReleaseDate(sentenceStatus.getActualReleaseDate())
                .length(sentenceStatus.getLength())
                .lengthUnits(sentenceStatus.getLengthUnits())
                .custody(CustodyDetail.builder()
                    .custodialType(sentenceStatus.getCustodialType())
                    .licenceExpiryDate(sentenceStatus.getLicenceExpiryDate())
                    .pssEndDate(sentenceStatus.getPssEndDate())
                    .build());

        return builder.build();
    }

    private SentenceResponse combineAttendancesAndUnpaidwork(List<AttendanceResponse> attendanceResponses, Conviction conviction) {
        return SentenceResponse.builder()
                .attendances(attendanceResponses)
                .unpaidWork(Optional.ofNullable(conviction.getSentence()).map(Sentence::getUnpaidWork).orElse(null))
                .build();
    }


}
