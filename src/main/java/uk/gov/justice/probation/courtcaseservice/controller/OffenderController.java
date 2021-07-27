package uk.gov.justice.probation.courtcaseservice.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api(tags = "Offender Resources", produces = APPLICATION_JSON_VALUE)
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

    @ApiOperation(value = "Gets the offender probation record by CRN")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found. For example if the CRN can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path="offender/{crn}/probation-record", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    ProbationRecord getProbationRecord(@ApiParam(name = "crn", value = "CRN for the offender", example = "X320741", required = true) @UpperCasePathVariable("crn") String crn,
        @ApiParam(name = "applyDocTypeFilter", value = "Whether or not to apply document filter, optional and defaults to true", example = "true")
        @RequestParam(value="applyDocTypeFilter", required = false, defaultValue = "true") boolean applyDocTypeFilter) {
        return offenderService.getProbationRecord(crn, applyDocTypeFilter);
    }

    @ApiOperation(value = "Gets the basic offender probation status details by CRN")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = ProbationStatusDetail.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found. For example if the CRN can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path="offender/{crn}/probation-status-detail", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    Mono<ProbationStatusDetail> getProbationStatusDetail(@ApiParam(name = "crn", value = "CRN for the offender", example = "X320741", required = true) @UpperCasePathVariable("crn") String crn) {
        return offenderService.getProbationStatus(crn);
    }

    @ApiOperation(value = "Gets the offender detail by CRN")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderDetail.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found. For example if the CRN can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path="offender/{crn}/detail", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    Mono<OffenderDetail> getOffenderDetail(@ApiParam(name = "crn", value = "CRN for the offender", example = "X320741", required = true) @UpperCasePathVariable("crn") String crn) {
        return offenderService.getOffenderDetail(crn);
    }

    @ApiOperation(value = "Gets the conviction by CRN and conviction ID.")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = Conviction.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found. For example if the CRN can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path="offender/{crn}/convictions/{convictionId}", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    Mono<Conviction> getConviction(@ApiParam(name = "crn", value = "CRN for the offender", example = "X320741", required = true) @UpperCasePathVariable("crn") String crn,
        @ApiParam(name = "convictionId", value = "Conviction Id", example = "12312322", required = true) @NotNull @PathVariable Long convictionId) {
        return offenderService.getConviction(crn, convictionId);
    }

    @GetMapping(value = "/offender/{crn}/convictions/{convictionId}/sentences/{sentenceId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Return the Sentence detail with attendances, Unpaid Work and current order details for a CRN, conviction id and sentence id  where enforcement is flagged")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not found. For example if the CRN can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    public SentenceResponse getSentence(@UpperCasePathVariable("crn") String crn, @PathVariable Long convictionId, @PathVariable Long sentenceId) {
        if (!featureFlags.sentenceData()) {
            return convictionService.getConvictionOnly(crn, convictionId);
        }
        return convictionService.getSentence(crn, convictionId);
    }

    @GetMapping(value = "/offender/{crn}/convictions/{convictionId}/sentence", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Return the Sentence detail with attendances, Unpaid Work and current order details for a CRN, conviction id and sentence id  where enforcement is flagged")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not found. For example if the CRN can't be matched.", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    public SentenceResponse getSentence(@UpperCasePathVariable("crn") String crn, @PathVariable Long convictionId) {
        if (!featureFlags.sentenceData()) {
            return convictionService.getConvictionOnly(crn, convictionId);
        }
        return convictionService.getSentence(crn, convictionId);
    }

    @GetMapping(value = "/offender/{crn}/convictions/{convictionId}/sentence/custody", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Return custody data for a sentence", tags = "prepare-a-case")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not found. For example if the CRN can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    public @ResponseBody Mono<Custody> getCustody(@UpperCasePathVariable("crn") String crn, @PathVariable Long convictionId) {
        return custodyService.getCustody(crn, convictionId);
    }

    @ApiOperation(value = "Gets Breach data by CRN, conviction ID and breach id.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not Found. For example if the CRN can't be matched.", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path="offender/{crn}/convictions/{convictionId}/breaches/{breachId}", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    BreachResponse getBreach(@UpperCasePathVariable("crn") String crn, @PathVariable Long convictionId, @PathVariable Long breachId) {
        return breachService.getBreach(crn, convictionId, breachId);
    }

    @ApiOperation(value = "Gets a document by ID for a CRN.")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found. If the CRN can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path="offender/{crn}/documents/{documentId}")
    public HttpEntity<Resource> getOffenderDocumentByCrn(
        @ApiParam(name = "crn", value = "CRN for the offender", example = "X320741", required = true) @NotNull final @UpperCasePathVariable("crn") String crn,
        @ApiParam(name = "documentId", value = "Document Id", example = "12312322", required = true) @NotNull final @PathVariable("documentId") String documentId) {

        return Optional.ofNullable(documentService.getDocument(crn, documentId))
            .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @ApiOperation(value = "Gets the offender risk registrations by CRN")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found. For example if the CRN can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path="offender/{crn}/registrations", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    Mono<List<Registration>> getOffenderRegistrations(@ApiParam(name = "crn", value = "CRN for the offender", example = "X320741", required = true) @UpperCasePathVariable("crn") String crn) {
        return offenderService.getOffenderRegistrations(crn);
    }
}
