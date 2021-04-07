package uk.gov.justice.probation.courtcaseservice.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import uk.gov.justice.probation.courtcaseservice.restclient.AssessmentsRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.DocumentRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;
import uk.gov.justice.probation.courtcaseservice.service.model.Breach;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.ConvictionBySentenceComparator;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.Registration;
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
                var convictionId = conviction.getConvictionId();
                log.debug("getting breaches for crn {} and conviction id {}", crn, convictionId);
                return offenderRestClient.getBreaches(crn, convictionId)
                    .map(breaches -> addBreachesToConviction(conviction, breaches));
            })
            .collectSortedList(new ConvictionBySentenceComparator()
                                .thenComparing(Conviction::getConvictionId));

        // This Mono resolves to a 3 tuple containing the record itself, the above-mentioned convictions, and the documents
        Mono<Tuple3<ProbationRecord, List<Conviction>, GroupedDocuments>> probationMono = Mono.zip(
            offenderRestClient.getProbationRecordByCrn(crn),
            convictions,
            documentRestClient.getDocumentsByCrn(crn)
        );
        Mono<List<Assessment>> assessmentsMono = assessmentsClient.getAssessmentsByCrn(crn);

        var tuple3 = probationMono.blockOptional().orElseThrow(() -> new OffenderNotFoundException(crn));
        ProbationRecord probationRecord = addConvictionsToProbationRecord(tuple3.getT1(), tuple3.getT2());
        combineConvictionsAndDocuments(probationRecord, tuple3.getT3().getConvictions(), applyDocumentFilter);

        // The code below handles 2 different classes of exceptions which could be thrown when the mono is resolved.
        // Currently the error is ignored in both cases. However there is an ongoing discussion about how we should
        // populate the response based on the type of error we encounter - see PIC-432 for more details.
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

    public Mono<Conviction> getConviction(final String crn, final Long convictionId) {
        Mono<Tuple3<Conviction, List<Breach>, GroupedDocuments>> convictionMono = Mono.zip(
            convictionRestClient.getConviction(crn, convictionId),
            offenderRestClient.getBreaches(crn, String.valueOf(convictionId)),
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

    private void combineConvictionsAndDocuments(final ProbationRecord probationRecord, final List<ConvictionDocuments> convictionDocuments, boolean applyDocumentFilter) {

        final ConcurrentMap<String, List<OffenderDocumentDetail>> allConvictionDocuments = groupFilteredDocuments(convictionDocuments, applyDocumentFilter);
        probationRecord.getConvictions()
            .forEach((conviction) -> {
                final String convictionId = conviction.getConvictionId();
                conviction.setDocuments(allConvictionDocuments.getOrDefault(convictionId, Collections.emptyList()));
            });
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

    private ProbationRecord addConvictionsToProbationRecord(ProbationRecord probationRecord, List<Conviction> convictions) {
        probationRecord.setConvictions(convictions);
        return probationRecord;
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

}
