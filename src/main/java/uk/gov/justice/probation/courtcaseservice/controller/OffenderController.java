package uk.gov.justice.probation.courtcaseservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.SentenceResponse;
import uk.gov.justice.probation.courtcaseservice.service.BreachService;
import uk.gov.justice.probation.courtcaseservice.service.ConvictionService;
import uk.gov.justice.probation.courtcaseservice.service.CustodyService;
import uk.gov.justice.probation.courtcaseservice.service.DocumentService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Custody;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.Registration;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Offender Resources")
@RestController
@AllArgsConstructor
public class OffenderController {

    @Autowired
    private final OffenderService offenderService;

    @Autowired
    private final ConvictionService convictionService;

    @Autowired
    private final BreachService breachService;

    @Autowired
    private final FeatureFlags featureFlags;

    @Autowired
    private final DocumentService documentService;

    @Autowired
    private CustodyService custodyService;

    @Operation(summary = "Gets the offender probation record by CRN")
    @GetMapping(path="offender/{crn}/probation-record", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    ProbationRecord getProbationRecord(@Parameter(name = "crn", description = "CRN for the offender", example = "X320741", required = true) @UpperCasePathVariable("crn") String crn,
        @Parameter(name = "applyDocTypeFilter", description = "Whether or not to apply document filter, optional and defaults to true", example = "true")
        @RequestParam(value="applyDocTypeFilter", required = false, defaultValue = "true") boolean applyDocTypeFilter) {
        return offenderService.getProbationRecord(crn, applyDocTypeFilter);
    }

    @Operation(summary = "Gets the basic offender probation status details by CRN")
    @GetMapping(path="offender/{crn}/probation-status-detail", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    Mono<ProbationStatusDetail> getProbationStatusDetail(@Parameter(name = "crn", description = "CRN for the offender", example = "X320741", required = true) @UpperCasePathVariable("crn") String crn) {
        return offenderService.getProbationStatus(crn);
    }

    @Operation(summary = "Gets the offender detail by CRN")
    @GetMapping(path="offender/{crn}/detail", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    Mono<OffenderDetail> getOffenderDetail(@Parameter(name = "crn", description = "CRN for the offender", example = "X320741", required = true) @UpperCasePathVariable("crn") String crn) {
        return offenderService.getOffenderDetail(crn);
    }

    @Operation(summary = "Gets the conviction by CRN and conviction ID.")
    @GetMapping(path="offender/{crn}/convictions/{convictionId}", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    Mono<Conviction> getConviction(@Parameter(name = "crn", description = "CRN for the offender", example = "X320741", required = true) @UpperCasePathVariable("crn") String crn,
        @Parameter(name = "convictionId", description = "Conviction Id", example = "12312322", required = true) @NotNull @PathVariable Long convictionId) {
        return offenderService.getConviction(crn, convictionId);
    }

    @GetMapping(value = "/offender/{crn}/convictions/{convictionId}/sentence", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Return the Sentence detail with attendances, Unpaid Work and current order details for a CRN, conviction id and sentence id  where enforcement is flagged")
    public SentenceResponse getSentence(@UpperCasePathVariable("crn") String crn, @PathVariable Long convictionId) {
        if (!featureFlags.sentenceData()) {
            return convictionService.getConvictionOnly(crn, convictionId);
        }
        return convictionService.getSentence(crn, convictionId);
    }

    @GetMapping(value = "/offender/{crn}/convictions/{convictionId}/sentence/custody", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Return custody data for a sentence", tags = "prepare-a-case")
    public @ResponseBody Mono<Custody> getCustody(@UpperCasePathVariable("crn") String crn, @PathVariable Long convictionId) {
        return custodyService.getCustody(crn, convictionId);
    }

    @Operation(summary = "Gets Breach data by CRN, conviction ID and breach id.")
    @GetMapping(path="offender/{crn}/convictions/{convictionId}/breaches/{breachId}", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    BreachResponse getBreach(@UpperCasePathVariable("crn") String crn, @PathVariable Long convictionId, @PathVariable Long breachId) {
        return breachService.getBreach(crn, convictionId, breachId);
    }

    @Operation(summary = "Gets a document by ID for a CRN.")
    @GetMapping(path="offender/{crn}/documents/{documentId}")
    public HttpEntity<Resource> getOffenderDocumentByCrn(
        @Parameter(name = "crn", description = "CRN for the offender", example = "X320741", required = true) @NotNull final @UpperCasePathVariable("crn") String crn,
        @Parameter(name = "documentId", description = "Document Id", example = "12312322", required = true) @NotNull final @PathVariable("documentId") String documentId) {

        return Optional.ofNullable(documentService.getDocument(crn, documentId))
            .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @Operation(summary = "Gets the offender risk registrations by CRN")
    @GetMapping(path="offender/{crn}/registrations", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    Mono<List<Registration>> getOffenderRegistrations(@Parameter(name = "crn", description = "CRN for the offender", example = "X320741", required = true) @UpperCasePathVariable("crn") String crn) {
        return offenderService.getOffenderRegistrations(crn);
    }
}
