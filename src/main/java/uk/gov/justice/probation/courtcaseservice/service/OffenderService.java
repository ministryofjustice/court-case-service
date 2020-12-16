package uk.gov.justice.probation.courtcaseservice.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.controller.model.RequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.AssessmentsRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.DocumentRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;
import uk.gov.justice.probation.courtcaseservice.service.model.Breach;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.ConvictionBySentenceComparator;
import uk.gov.justice.probation.courtcaseservice.service.model.LicenceCondition;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.PssRequirement;
import uk.gov.justice.probation.courtcaseservice.service.model.Registration;
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

    @Setter
    @Value("#{'${offender-service.assessment.included-statuses}'.split(',')}")
    private List<String> assessmentStatuses;

    @Setter
    @Value("#{'${offender-service.pss-rqmnt.descriptions-to-keep-subtype}'.split(',')}")
    private List<String> pssRqmntDescriptionsKeepSubType;

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
                        conviction.setBreaches(breaches.stream()
                                                .sorted(Comparator.comparing(Breach::getStatusDate, Comparator.nullsLast(Comparator.reverseOrder())))
                                                .collect(Collectors.toList()));
                        return conviction;
                    });
            })
            .collectSortedList(new ConvictionBySentenceComparator()
                                .thenComparing(Conviction::getConvictionId));

        // This Mono resolves to a 3 tuple containing the record itself, the above-mentioned convictions, and the documents
        Mono<Tuple3<ProbationRecord, List<Conviction>, GroupedDocuments>> probationMono = Mono.zip(
            defaultClient.getProbationRecordByCrn(crn),
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

    public Mono<RequirementsResponse> getConvictionRequirements(String crn, String convictionId) {

        return Mono.zip(defaultClient.getConvictionRequirements(crn, convictionId),
                        defaultClient.getConvictionPssRequirements(crn, convictionId),
                        defaultClient.getConvictionLicenceConditions(crn, convictionId)
        )
                .map(this::combineAndFilterRequirements);
    }

    RequirementsResponse combineAndFilterRequirements(Tuple3<List<Requirement>, List<PssRequirement>, List<LicenceCondition>> tuple3) {

        return RequirementsResponse.builder()
            .requirements(tuple3.getT1())
            .pssRequirements(tuple3.getT2()
                                .stream()
                                .filter(PssRequirement::isActive)
                                .map(this::transform)
                                .collect(Collectors.toList()))
            .licenceConditions(tuple3.getT3()
                                .stream()
                                .filter(LicenceCondition::isActive)
                                .collect(Collectors.toList()))
            .build();
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

    public Mono<OffenderDetail> getOffenderDetail(String crn) {
        return defaultClient.getOffenderDetailByCrn(crn);
    }

    public Mono<List<Registration>> getOffenderRegistrations(String crn) {
        return defaultClient.getOffenderRegistrations(crn);
    }

    private Optional<Assessment> findMostRecentByStatus(List<Assessment> assessments) {
        return assessments.stream()
            .filter(assessment -> assessmentStatuses.contains(assessment.getStatus()))
            .max(Comparator.comparing(Assessment::getCompleted));
    }

    public Mono<ProbationStatusDetail> getProbationStatusDetail(String crn) {
        return Mono.zip(defaultClient.getConvictionsByCrn(crn), defaultClient.getOffenderDetailByCrn(crn))
            .map((t) -> combineProbationStatusDetail(t.getT2(), t.getT1()));
    }

    ProbationStatusDetail combineProbationStatusDetail(OffenderDetail offenderDetail, List<Conviction> convictions) {
        var builder = ProbationStatusDetail.builder().probationStatus(offenderDetail.getProbationStatus());

        if (offenderDetail.getProbationStatus() == ProbationStatus.PREVIOUSLY_KNOWN) {
            LocalDate localDate = Optional.ofNullable(convictions).orElse(Collections.emptyList()).stream()
                .filter(conviction -> conviction.getSentenceTerminationDate().isPresent())
                .max(Comparator.comparing(s -> s.getSentenceTerminationDate().orElse(LocalDate.MIN).toEpochDay()))
                .map(Conviction::getSentenceTerminationDate)
                .get()
                .orElse(null);
            builder.previouslyKnownTerminationDate(localDate);
        }

        if (offenderDetail.getProbationStatus() == ProbationStatus.CURRENT) {
            builder.inBreach(Optional.ofNullable(convictions)
                                    .orElse(Collections.emptyList())
                                    .stream()
                                    .anyMatch(conviction -> conviction.getActive() != null && conviction.getActive()
                                                            && conviction.getInBreach() != null && conviction.getInBreach()));
        }

        return builder.build();
    }
}
