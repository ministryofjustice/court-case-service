package uk.gov.justice.probation.courtcaseservice.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import uk.gov.justice.probation.courtcaseservice.restclient.AssessmentsRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.DocumentRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;
import uk.gov.justice.probation.courtcaseservice.service.model.document.ConvictionDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

@Service
@Slf4j
public class OffenderService {

    private final OffenderRestClient defaultClient;
    private final AssessmentsRestClient assessmentsClient;
    private final DocumentRestClient documentRestClient;

    private final Predicate<OffenderDocumentDetail> documentTypeFilter;

    public OffenderService(final OffenderRestClient defaultClient,
                           final AssessmentsRestClient assessmentsClient,
                           final DocumentRestClient documentRestClient,
                           final DocumentTypeFilter documentTypeFilter) {
        this.defaultClient = defaultClient;
        this.assessmentsClient = assessmentsClient;
        this.documentRestClient = documentRestClient;
        this.documentTypeFilter = documentTypeFilter;
    }

    public ProbationRecord getProbationRecord(String crn, boolean applyDocumentFilter) {
        // FIXME: the reactive code in this method could be written in a more idiomatic way

        // The handling of the probation record data is split into two parts to allow different
        // behaviour depending on whether the data is missing from the community api (delius) or
        // the assessments api (oasys). In the latter case we can still get most of the important
        // information to populate the response so do not need to throw an exception. In this
        // sense, the oasys assessment data is optional.

        // This Mono resolves to a list of convictions including a list of breaches for each conviction
        Mono<List<Conviction>> convictions = defaultClient.getConvictionsByCrn(crn)
            .flatMapMany(Flux::fromIterable)
            .flatMap(conviction -> {
                var convictionId = conviction.getConvictionId();
                log.debug("getting breaches for crn {} and conviction id {}", crn, convictionId);
                return defaultClient.getBreaches(crn, convictionId)
                    .map(breaches -> {
                        conviction.setBreaches(breaches);
                        return conviction;
                    });
            })
            .collectSortedList();

        // This Mono resolves to a 3 tuple containing the record itself, the above-mentioned convictions, and the documents
        Mono<Tuple3<ProbationRecord, List<Conviction>, GroupedDocuments>> probationMono = Mono.zip(
            defaultClient.getProbationRecordByCrn(crn),
            convictions,
            documentRestClient.getDocumentsByCrn(crn)
        );
        Mono<Assessment> assessmentMono = assessmentsClient.getAssessmentByCrn(crn);

        var tuple3 = probationMono.blockOptional().orElseThrow(() -> new OffenderNotFoundException(crn));
        ProbationRecord probationRecord = addConvictionsToProbationRecord(tuple3.getT1(), tuple3.getT2());
        combineConvictionsAndDocuments(probationRecord, tuple3.getT3().getConvictions(), applyDocumentFilter);

        // The code below handles 2 different classes of exceptions which could be thrown when the mono is resolved.
        // Currently the error is ignored in both cases. However there is an ongoing discussion about how we should
        // populate the response based on the type of error we encounter - see PIC-432 for more details.
        try {
            assessmentMono.blockOptional().ifPresent(assessment -> probationRecord.setAssessment(assessment));
        } catch (OffenderNotFoundException e) {
            log.info("assessment data missing from probation record (CRN '{}' not found in oasys)", crn);
        } catch (Exception e) {
            log.warn("assessment data missing from probation record for CRN '{}': {}", crn, e.toString());
        }

        return probationRecord;
    }

    private void combineConvictionsAndDocuments(final ProbationRecord probationRecord, final List<ConvictionDocuments> convictionDocuments, boolean applyDocumentFilter) {

        final ConcurrentMap<String, List<OffenderDocumentDetail>> allConvictionDocuments = convictionDocuments.stream()
                                .map(convictionDocument -> ConvictionDocuments.builder()
                                                                .convictionId(convictionDocument.getConvictionId())
                                                                .documents(convictionDocument.getDocuments().stream()
                                                                            .filter(doc -> (!applyDocumentFilter || documentTypeFilter.test(doc)))
                                                                            .collect(Collectors.toList()))
                                                                .build())
                                .collect(Collectors.toConcurrentMap(ConvictionDocuments::getConvictionId, ConvictionDocuments::getDocuments));

        probationRecord.getConvictions()
            .forEach((conviction) -> {
                final String convictionId = conviction.getConvictionId();
                conviction.setDocuments(allConvictionDocuments.getOrDefault(convictionId, Collections.emptyList()));
            });
    }

    private ProbationRecord addConvictionsToProbationRecord(ProbationRecord probationRecord, List<Conviction> convictions) {
        probationRecord.setConvictions(convictions);
        return probationRecord;
    }

    public List<Requirement> getConvictionRequirements(String crn, String convictionId) {
        return defaultClient.getConvictionRequirements(crn, convictionId).block();
    }

    public Mono<OffenderDetail> getOffenderDetail(String crn) {
        return defaultClient.getOffenderDetailByCrn(crn);
    }
}
