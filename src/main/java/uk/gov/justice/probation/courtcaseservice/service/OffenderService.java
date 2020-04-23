package uk.gov.justice.probation.courtcaseservice.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import uk.gov.justice.probation.courtcaseservice.restclient.AssessmentsRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
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

    private final Predicate<OffenderDocumentDetail> documentTypeFilter;

    public OffenderService(final OffenderRestClient defaultClient,
                           final AssessmentsRestClient assessmentsClient,
                           final DocumentTypeFilter documentTypeFilter) {
        this.defaultClient = defaultClient;
        this.assessmentsClient = assessmentsClient;
        this.documentTypeFilter = documentTypeFilter;
    }

    public ProbationRecord getProbationRecord(String crn, boolean applyDocumentFilter) {
        // These calls are split into 2 monos to allow different behaviour depending on whether the data
        // is missing from the community api (delius) or the assessments api (oasys). In the latter case
        // we can still get most of the important information to populate the response so do not need to
        // throw an exception. In this sense, the oasys assessment data is optional.
        Mono<Tuple3<ProbationRecord, List<Conviction>, GroupedDocuments>> probationMono = Mono.zip(
            defaultClient.getProbationRecordByCrn(crn),
            defaultClient.getConvictionsByCrn(crn),
            defaultClient.getDocumentsByCrn(crn)
        );
        Mono<Assessment> assessmentMono = assessmentsClient.getAssessmentByCrn(crn);

        var tuple3 = probationMono.blockOptional().orElseThrow(() -> new OffenderNotFoundException(crn));
        ProbationRecord probationRecord = addConvictionsToProbationRecord(tuple3.getT1(), tuple3.getT2());
        combineConvictionsAndDocuments(probationRecord, tuple3.getT3().getConvictions(), applyDocumentFilter);

        try {
            assessmentMono.blockOptional().ifPresent(assessment -> probationRecord.setAssessment(assessment));
        } catch (Exception e) {
            // See comment above. We are catching general exception here since it doesn't matter how the request
            // failed. Note that an error is already logged by the rest client, so we don't need to log anything here.
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

}
