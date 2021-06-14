package uk.gov.justice.probation.courtcaseservice.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import uk.gov.justice.probation.courtcaseservice.controller.model.RequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.AssessmentsRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.DocumentRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;
import uk.gov.justice.probation.courtcaseservice.service.model.Breach;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.ConvictionBySentenceComparator;
import uk.gov.justice.probation.courtcaseservice.service.model.CustodialStatus;
import uk.gov.justice.probation.courtcaseservice.service.model.LicenceCondition;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderManager;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.PssRequirement;
import uk.gov.justice.probation.courtcaseservice.service.model.Registration;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;
import uk.gov.justice.probation.courtcaseservice.service.model.document.ConvictionDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequestScope
public class OffenderService {

    private final OffenderRestClient offenderRestClient;
    private final AssessmentsRestClient assessmentsClient;
    private final DocumentRestClient documentRestClient;
    private final ConvictionRestClient convictionRestClient;
    private final TelemetryService telemetryService;

    private final Predicate<OffenderDocumentDetail> documentTypeFilter;

    @Setter
    @Value("#{'${offender-service.assessment.included-statuses}'.split(',')}")
    private List<String> assessmentStatuses;

    @Setter
    @Value("#{'${offender-service.pss-rqmnt.descriptions-to-keep-subtype}'.split(',')}")
    private List<String> pssRqmntDescriptionsKeepSubType;

    public OffenderService(final OffenderRestClientFactory offenderRestClientFactory,
                           final AssessmentsRestClient assessmentsClient,
                           final ConvictionRestClient convictionRestClient,
                           final DocumentRestClient documentRestClient,
                           final DocumentTypeFilter documentTypeFilter,
                           final TelemetryService telemetryService) {
        this.offenderRestClient = offenderRestClientFactory.build();
        this.convictionRestClient = convictionRestClient;
        this.assessmentsClient = assessmentsClient;
        this.documentRestClient = documentRestClient;
        this.documentTypeFilter = documentTypeFilter;
        this.telemetryService = telemetryService;
    }

    public ProbationRecord getProbationRecord(String crn, boolean applyDocumentFilter) {
        // FIXME: the reactive code in this method could be written in a more idiomatic way

        // The handling of the probation record data is split into two parts to allow different
        // behaviour depending on whether the data is missing from the community api (delius) or
        // the assessments api (oasys). In the latter case we can still get most of the important
        // information to populate the response so do not need to throw an exception. In this
        // sense, the oasys assessment data is optional.

        // This Mono resolves to a list of convictions including a list of breaches for each conviction
        Mono<List<Conviction>> convictions = offenderRestClient.getConvictionsByCrn(crn)
            .flatMapMany(Flux::fromIterable)
            .flatMap(conviction -> {
                log.debug("getting breaches and requirements for crn {} and conviction id {}", crn, conviction.getConvictionId());
                return enrichConviction(crn, conviction);
            })
            .collectSortedList(new ConvictionBySentenceComparator()
                                .thenComparing(Conviction::getConvictionId));

        // This Mono resolves to a 4 tuple containing the convictions (see above), offender managers and CRN documents
        // As a check against exclusions only we call getOffender which will return 403 status
        Mono<Tuple4<List<Conviction>, List<OffenderManager>, GroupedDocuments, CommunityApiOffenderResponse>> communityApiMono = Mono.zip(
            convictions,
            offenderRestClient.getOffenderManagers(crn),
            documentRestClient.getDocumentsByCrn(crn),
            offenderRestClient.getOffender(crn)
        );

        var tuple4 = communityApiMono.blockOptional().orElseThrow(() -> new OffenderNotFoundException(crn));
        var probationRecord = buildProbationRecord(crn, tuple4.getT1(), tuple4.getT2(), tuple4.getT3().getConvictions(), applyDocumentFilter);

        // The code below handles 2 different classes of exceptions which could be thrown when the mono is resolved.
        // Currently the error is ignored in both cases. However there is an ongoing discussion about how we should
        // populate the response based on the type of error we encounter - see PIC-432 for more details.
        Mono<List<Assessment>> assessmentsMono = assessmentsClient.getAssessmentsByCrn(crn);
        try {
            assessmentsMono.blockOptional().ifPresent(assessments -> {
                probationRecord.setAssessment(findMostRecentByStatus(assessments).orElse(null));
            });
        } catch (OffenderNotFoundException e) {
            telemetryService.trackApplicationDegradationEvent("assessment data missing from probation record (CRN '" + crn + "' not found in oasys)", e, crn);
        } catch (Exception e) {
            telemetryService.trackApplicationDegradationEvent("call failed to get assessment data for for CRN '" + crn + "'", e, crn);
        }

        return probationRecord;
    }

    private Mono<Conviction> enrichConviction(String crn, Conviction conviction) {

        var convictionId = Long.valueOf(conviction.getConvictionId());
        var enrichedConvictionMono = Mono.zip(
            offenderRestClient.getBreaches(crn, convictionId),
            getConvictionRequirements(crn, convictionId));

        return enrichedConvictionMono.map(tuple2 -> {
            addBreachesToConviction(conviction, tuple2.getT1());
            addRequirementsToConviction(conviction, tuple2.getT2());
            return conviction;
        });
    }

    private ProbationRecord buildProbationRecord(String crn, List<Conviction> convictions, List<OffenderManager> offenderManagers, List<ConvictionDocuments> convictionDocuments, final boolean applyDocumentFilter) {

        final ConcurrentMap<String, List<OffenderDocumentDetail>> allConvictionDocuments = groupFilteredDocuments(convictionDocuments, applyDocumentFilter);
        convictions
            .forEach((conviction) -> {
                final String convictionId = conviction.getConvictionId();
                conviction.setDocuments(allConvictionDocuments.getOrDefault(convictionId, Collections.emptyList()));
            });

        return ProbationRecord.builder()
            .crn(crn)
            .convictions(convictions)
            .offenderManagers(offenderManagers)
            .build();
    }

    public Mono<Conviction> getConviction(final String crn, final Long convictionId) {
        Mono<Tuple3<Conviction, List<Breach>, GroupedDocuments>> convictionMono = Mono.zip(
            convictionRestClient.getConviction(crn, convictionId),
            offenderRestClient.getBreaches(crn, convictionId),
            documentRestClient.getDocumentsByCrn(crn));

        return convictionMono.map(tuple3 -> {
            final Conviction conviction = tuple3.getT1();
            addBreachesToConviction(conviction, tuple3.getT2());
            final ConcurrentMap<String, List<OffenderDocumentDetail>> allConvictionDocuments = groupFilteredDocuments(tuple3.getT3().getConvictions(), true);
            conviction.setDocuments(allConvictionDocuments.getOrDefault(Long.toString(convictionId), Collections.emptyList()));
            return conviction;
        });
    }

    private Conviction addBreachesToConviction(Conviction conviction, List<Breach> breaches) {
        conviction.setBreaches(breaches.stream()
            .sorted(Comparator.comparing(Breach::getStatusDate, Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList()));
        return conviction;
    }

    private Conviction addRequirementsToConviction(Conviction conviction, RequirementsResponse requirements) {
        conviction.setRequirements(Optional.ofNullable(requirements.getRequirements()).orElse(Collections.emptyList()));
        conviction.setPssRequirements(Optional.ofNullable(requirements.getPssRequirements()).orElse(Collections.emptyList()));
        conviction.setLicenceConditions(Optional.ofNullable(requirements.getLicenceConditions()).orElse(Collections.emptyList()));
        return conviction;
    }

    private ConcurrentMap<String, List<OffenderDocumentDetail>> groupFilteredDocuments(final List<ConvictionDocuments> convictionDocuments, boolean applyDocumentFilter) {
        return convictionDocuments.stream()
            .map(convictionDocument -> ConvictionDocuments.builder()
                .convictionId(convictionDocument.getConvictionId())
                .documents(convictionDocument.getDocuments().stream()
                    .filter(doc -> (!applyDocumentFilter || documentTypeFilter.test(doc)))
                    .collect(Collectors.toList()))
                .build())
            .collect(Collectors.toConcurrentMap(ConvictionDocuments::getConvictionId, ConvictionDocuments::getDocuments));
    }

    public Mono<OffenderDetail> getOffenderDetail(String crn) {
        return Mono.zip(offenderRestClient.getOffender(crn), offenderRestClient.getProbationStatusByCrn(crn))
                .map((tuple2) -> OffenderMapper.offenderDetailFrom(tuple2.getT1(), tuple2.getT2()));
    }

    public Mono<List<Registration>> getOffenderRegistrations(String crn) {
        return offenderRestClient.getOffenderRegistrations(crn);
    }

    private Optional<Assessment> findMostRecentByStatus(List<Assessment> assessments) {
        return assessments.stream()
            .filter(assessment -> assessmentStatuses.contains(assessment.getStatus()))
            .max(Comparator.comparing(Assessment::getCompleted));
    }

    public Mono<ProbationStatusDetail> getProbationStatus(String crn) {
        return offenderRestClient.getProbationStatusByCrn(crn);
    }

    public Mono<RequirementsResponse> getConvictionRequirements(String crn, Long convictionId) {

        return Mono.zip(offenderRestClient.getConvictionRequirements(crn, convictionId),
            convictionRestClient.getCustodialStatus(crn, convictionId),
            offenderRestClient.getConvictionPssRequirements(crn, convictionId),
            offenderRestClient.getConvictionLicenceConditions(crn, convictionId)
        )
            .map(this::combineAndFilterRequirements);
    }

    RequirementsResponse combineAndFilterRequirements(Tuple4<List<Requirement>, CustodialStatus, List<PssRequirement>, List<LicenceCondition>> tuple4) {

        var builder = RequirementsResponse.builder()
            .requirements(tuple4.getT1())
            .licenceConditions(Collections.emptyList())
            .pssRequirements(Collections.emptyList());

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

    PssRequirement transform(PssRequirement pssRequirement) {
        var description = Optional.ofNullable(pssRequirement.getDescription()).map(String::toLowerCase).orElse("");
        if (pssRqmntDescriptionsKeepSubType.contains(description)) {
            return pssRequirement;
        }
        return PssRequirement.builder()
            .description(pssRequirement.getDescription())
            .active(pssRequirement.isActive())
            .build();
    }
}
