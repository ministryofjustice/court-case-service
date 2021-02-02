package uk.gov.justice.probation.courtcaseservice.service;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CurrentOrderHeaderResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.RequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.SentenceLinks;
import uk.gov.justice.probation.courtcaseservice.controller.model.SentenceResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.CustodialStatusNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.CustodialStatus;
import uk.gov.justice.probation.courtcaseservice.service.model.LicenceCondition;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.PssRequirement;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConvictionService {

    private final ConvictionRestClient convictionRestClient;
    private final OffenderRestClient offenderRestClient;
    private final String deliusContactListUrlTemplate;

    @Setter
    @Value("#{'${offender-service.pss-rqmnt.descriptions-to-keep-subtype}'.split(',')}")
    private List<String> pssRqmntDescriptionsKeepSubType;

    @Autowired
    public ConvictionService(final ConvictionRestClient client,
                             final OffenderRestClientFactory offenderRestClientFactory,
                             @Value("${delius.contact-list-url-template}") final String deliusContactListUrlTemplate) {
        this.convictionRestClient = client;
        this.offenderRestClient = offenderRestClientFactory.build();
        this.deliusContactListUrlTemplate = deliusContactListUrlTemplate;
    }

    public SentenceResponse getConvictionOnly(final String crn, final Long convictionId) {
        return convictionRestClient.getConviction(crn, convictionId)
                .blockOptional()
                .map(conviction -> combineAttendancesAndUnpaidwork(Collections.emptyList(), conviction))
                .orElseThrow(() -> new OffenderNotFoundException(crn));
    }

    public SentenceResponse getSentence(final String crn, final Long convictionId, final Long sentenceId) {
        var sentenceMono = Mono.zip(
                convictionRestClient.getAttendances(crn, convictionId),
                convictionRestClient.getConviction(crn, convictionId),
                convictionRestClient.getCurrentOrderHeader(crn, convictionId),
                offenderRestClient.getOffenderDetailByCrn(crn)
        );

        var tuple4 = sentenceMono.blockOptional().orElseThrow(() -> new OffenderNotFoundException(crn));

        return combineOffenderAndConvictions(tuple4.getT1(), tuple4.getT2(), tuple4.getT3(), tuple4.getT4());
    }

    public CurrentOrderHeaderResponse getCurrentOrderHeader(final String crn, final Long convictionId) {
        return convictionRestClient.getCurrentOrderHeader(crn, convictionId)
                .blockOptional()
                .orElseThrow(() -> new CustodialStatusNotFoundException(crn));
    }

    public Mono<RequirementsResponse> getConvictionRequirements(String crn, Long convictionId) {

        return Mono.zip(offenderRestClient.getConvictionRequirements(crn, convictionId),
            convictionRestClient.getCustodialStatus(crn, convictionId),
            offenderRestClient.getConvictionPssRequirements(crn, convictionId),
            offenderRestClient.getConvictionLicenceConditions(crn, convictionId)
        )
            .map(this::combineAndFilterRequirements);
    }

    RequirementsResponse combineAndFilterRequirements(
        Tuple4<List<Requirement>, CustodialStatus, List<PssRequirement>, List<LicenceCondition>> tuple4) {

        var builder = RequirementsResponse.builder()
            .requirements(tuple4.getT1())
            .pssRequirements(Collections.emptyList())
            .licenceConditions(Collections.emptyList());

        switch (tuple4.getT2()) {
            case POST_SENTENCE_SUPERVISION:
                builder.pssRequirements(tuple4.getT3()
                    .stream()
                    .filter(PssRequirement::isActive)
                    .map(this::transform)
                    .collect(Collectors.toList()));
                break;
            case RELEASED_ON_LICENCE:
                builder.licenceConditions(tuple4.getT4()
                    .stream()
                    .filter(LicenceCondition::isActive)
                    .collect(Collectors.toList()));
                break;
            default:
                break;
        }
        return builder.build();
    }

    private PssRequirement transform(PssRequirement pssRequirement) {
        if (pssRqmntDescriptionsKeepSubType.contains(pssRequirement.getDescription().toLowerCase())) {
            return pssRequirement;
        }
        return PssRequirement.builder()
            .description(pssRequirement.getDescription())
            .active(pssRequirement.isActive())
            .build();
    }

    private SentenceResponse combineOffenderAndConvictions(List<AttendanceResponse> attendanceResponses, Conviction conviction, CurrentOrderHeaderResponse currentOrderHeaderResponse, OffenderDetail offenderDetail) {
        return SentenceResponse.builder()
                .attendances(attendanceResponses)
                .unpaidWork(Optional.ofNullable(conviction.getSentence()).map(Sentence::getUnpaidWork).orElse(null))
                .currentOrderHeaderDetail(currentOrderHeaderResponse)
                .links(SentenceLinks.builder()
                        .deliusContactList(String.format(deliusContactListUrlTemplate, offenderDetail.getOtherIds().getOffenderId(), conviction.getConvictionId()))
                        .build())
                .build();
    }

    private SentenceResponse combineAttendancesAndUnpaidwork(List<AttendanceResponse> attendanceResponses, Conviction conviction) {
        return SentenceResponse.builder()
                .attendances(attendanceResponses)
                .unpaidWork(Optional.ofNullable(conviction.getSentence()).map(Sentence::getUnpaidWork).orElse(null))
                .build();
    }


}
