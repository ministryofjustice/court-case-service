package uk.gov.justice.probation.courtcaseservice.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;
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
        Tuple4<ProbationRecord, List<Conviction>, GroupedDocuments, Assessment> tuple4 = Mono.zip(
            defaultClient.getProbationRecordByCrn(crn),
            defaultClient.getConvictionsByCrn(crn),
            defaultClient.getDocumentsByCrn(crn),
            assessmentsClient.getAssessmentByCrn(crn))
            .blockOptional()
            .orElseThrow(() -> new OffenderNotFoundException(crn));

        ProbationRecord probationRecord = addConvictionsToProbationRecord(tuple4.getT1(), tuple4.getT2());
        combineConvictionsAndDocuments(probationRecord, tuple4.getT3().getConvictions(), applyDocumentFilter);
        addAssessmentToProbationRecord(probationRecord, tuple4.getT4());

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

    private ProbationRecord addAssessmentToProbationRecord(ProbationRecord probationRecord, Assessment assessment) {
        probationRecord.setAssessment(assessment);
        return probationRecord;
    }

    public List<Requirement> getConvictionRequirements(String crn, String convictionId) {
        return defaultClient.getConvictionRequirements(crn, convictionId).block();
    }

}
