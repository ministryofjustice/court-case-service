package uk.gov.justice.probation.courtcaseservice.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import uk.gov.justice.probation.courtcaseservice.controller.model.RequirementsResponse;
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
import uk.gov.justice.probation.courtcaseservice.service.model.CourtReport;
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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@SuppressWarnings("ALL")
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

    @Setter
    @Value("#{'${offender-service.psr-report-codes}'.split(',')}")
    private List<String> psrTypeCodes;

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
        // This Mono resolves to a list of convictions including a list of breaches for each conviction
        var convictions = offenderRestClient.getConvictionsByCrn(crn)
            .flatMapMany(Flux::fromIterable)
            .flatMap(conviction -> {
                log.debug("getting breaches, requirements and PSR detail for crn {} and conviction id {}", crn, conviction.getConvictionId());
                return enrichConviction(crn, conviction);
            })
            .collectSortedList(new ConvictionBySentenceComparator()
                                .thenComparing(Conviction::getConvictionId));

        // This resolves to a 5 tuple containing the convictions (see above), offender managers, CRN documents and assessments
        // As a check against exclusions only we call getOffender which will return 403 status
        var zippedResponses = Mono.zip(
            convictions,
            offenderRestClient.getOffenderManagers(crn),
            documentRestClient.getDocumentsByCrn(crn),
            getAssessments(crn),
            offenderRestClient.getOffender(crn)
        ).blockOptional()
            .orElseThrow(() -> new OffenderNotFoundException(crn));

        return buildProbationRecord(
                crn,
                zippedResponses.getT1(),
                zippedResponses.getT2(),
                zippedResponses.getT3().getConvictions(),
                zippedResponses.getT4(),
                applyDocumentFilter
        );
    }

    private Mono<List<Assessment>> getAssessments(String crn) {
        return assessmentsClient.getAssessmentsByCrn(crn)
                // Degrade gracefully if the offender assessments call fails
                // Currently any error is ignored. However there is an ongoing discussion about how we should
                // populate the response based on the type of error we encounter - see PIC-432 for more details.
                .doOnError(OffenderNotFoundException.class,
                        e -> telemetryService.trackApplicationDegradationEvent("assessment data missing from probation record (CRN '" + crn + "' not found in oasys)", e, crn))
                .doOnError(Exception.class,
                        e -> telemetryService.trackApplicationDegradationEvent("call failed to get assessment data for for CRN '" + crn + "'", e, crn))
                .onErrorResume((e) -> Mono.just(emptyList()));
    }

    private Mono<Conviction> enrichConviction(String crn, Conviction conviction) {

        var convictionId = Long.valueOf(conviction.getConvictionId());
        var enrichedConvictionMono = Mono.zip(
            offenderRestClient.getBreaches(crn, convictionId),
            getConvictionRequirements(crn, conviction),
            getPsrDetail(crn, conviction));

        return enrichedConvictionMono.map(tuple3 -> {
            addBreachesToConviction(conviction, tuple3.getT1());
            addRequirementsToConviction(conviction, tuple3.getT2());
            addPsrCourtReportToConviction(conviction, tuple3.getT3());
            return conviction;
        });
    }

    private ProbationRecord buildProbationRecord(String crn, List<Conviction> convictions, List<OffenderManager> offenderManagers, List<ConvictionDocuments> convictionDocuments, List<Assessment> assessments, final boolean applyDocumentFilter) {

        final ConcurrentMap<String, List<OffenderDocumentDetail>> allConvictionDocuments = groupFilteredDocuments(convictionDocuments, applyDocumentFilter);
        convictions
            .forEach((conviction) -> {
                final String convictionId = conviction.getConvictionId();
                conviction.setDocuments(allConvictionDocuments.getOrDefault(convictionId, emptyList()));
            });

        return ProbationRecord.builder()
            .crn(crn)
            .convictions(convictions)
            .offenderManagers(offenderManagers)
            .assessment(findMostRecentByStatus(assessments).orElse(null))
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
                conviction.setDocuments(allConvictionDocuments.getOrDefault(Long.toString(convictionId), emptyList()));
                return conviction;
            })
            .flatMap(conviction -> applyRequirementsToConviction(crn, conviction));
    }

    private Conviction addBreachesToConviction(Conviction conviction, List<Breach> breaches) {
        conviction.setBreaches(breaches.stream()
            .sorted(Comparator.comparing(Breach::getStatusDate, Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList()));
        return conviction;
    }

    private Conviction addRequirementsToConviction(Conviction conviction, RequirementsResponse requirements) {
        conviction.setRequirements(Optional.ofNullable(requirements.getRequirements()).orElse(emptyList()));
        conviction.setPssRequirements(Optional.ofNullable(requirements.getPssRequirements()).orElse(emptyList()));
        conviction.setLicenceConditions(Optional.ofNullable(requirements.getLicenceConditions()).orElse(emptyList()));
        return conviction;
    }

    private Conviction addPsrCourtReportToConviction(Conviction conviction, List<CourtReport> courtReports) {
        conviction.setPsrReports(courtReports);
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

    private Mono<Conviction> applyRequirementsToConviction(String crn, Conviction conviction) {
        return getConvictionRequirements(crn, conviction)
            .map(rqmnts -> addRequirementsToConviction(conviction, rqmnts));
    }

    public Mono<RequirementsResponse> getConvictionRequirements(String crn, Conviction conviction) {

        if (conviction.getActive()) {
            final var convictionId = Long.valueOf(conviction.getConvictionId());
            final var custodialStatus = Optional.ofNullable(conviction.getCustodialType())
                .map(type -> CustodialStatus.fromString(type.getCode()))
                .orElse(CustodialStatus.UNKNOWN);

            final Mono<RequirementsResponse> res;
            switch (custodialStatus) {
                case POST_SENTENCE_SUPERVISION -> {
                    res = Mono.zip(offenderRestClient.getConvictionRequirements(crn, convictionId),
                        offenderRestClient.getConvictionPssRequirements(crn, convictionId))
                        .map(this::buildPssRequirements);
                }
                case RELEASED_ON_LICENCE -> {
                    res = Mono.zip(offenderRestClient.getConvictionRequirements(crn, convictionId),
                        offenderRestClient.getConvictionLicenceConditions(crn, convictionId))
                        .map(this::buildLicenceConditions);
                }
                default -> {
                    res = offenderRestClient.getConvictionRequirements(crn, convictionId)
                        .map(rqmnts -> RequirementsResponse.builder().requirements(rqmnts).build());
                }
            }
            return res;
        }
        return Mono.just(RequirementsResponse.builder().build());
    }

    public Mono<List<CourtReport>> getPsrDetail(String crn, Conviction conviction) {

        if (conviction.isAwaitingPsr()) {
            return convictionRestClient.getCourtReports(crn, Long.valueOf(conviction.getConvictionId()))
                .map(this::buildCourtReports);
        }
        return Mono.just(emptyList());
    }

    List<CourtReport> buildCourtReports(List<CourtReport> courtReports) {

        return Optional.ofNullable(courtReports).orElse(emptyList())
            .stream()
            .filter(report -> report.isTypeOneOf(psrTypeCodes))
            .collect(Collectors.toList());
    }

    RequirementsResponse buildPssRequirements(Tuple2<List<Requirement>, List<PssRequirement>> tuple) {
        return RequirementsResponse.builder()
            .requirements(tuple.getT1())
            .pssRequirements(tuple.getT2()
                .stream()
                .filter(PssRequirement::isActive)
                .map(this::transform)
                .collect(Collectors.toList()))
            .build();
    }

    RequirementsResponse buildLicenceConditions(Tuple2<List<Requirement>, List<LicenceCondition>> tuple) {
        return RequirementsResponse.builder()
            .requirements(tuple.getT1())
            .licenceConditions(tuple.getT2()
                .stream()
                .filter(LicenceCondition::isActive)
                .collect(Collectors.toList()))
            .build();
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
