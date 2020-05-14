package uk.gov.justice.probation.courtcaseservice.controller;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Optional;
import javax.validation.constraints.NotNull;
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
import uk.gov.justice.probation.courtcaseservice.controller.model.RequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.service.DocumentService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;

@Api(tags = "Offender Resources", produces = APPLICATION_JSON_VALUE)
@RestController
@AllArgsConstructor
public class OffenderController {

    @Autowired
    private OffenderService offenderService;

    @Autowired
    private DocumentService documentService;

    @ApiOperation(value = "Gets the offender probation record by CRN")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = ProbationRecord.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found. For example if the CRN can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path="offender/{crn}/probation-record", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    ProbationRecord getProbationRecord(@ApiParam(name = "crn", value = "CRN for the offender", example = "X320741", required = true) @PathVariable String crn,
        @ApiParam(name = "applyDocTypeFilter", value = "Whether or not to apply document filter, optional and defaults to true", example = "true")
        @RequestParam(value="applyDocTypeFilter", required = false, defaultValue = "true") boolean applyDocTypeFilter) {
        return offenderService.getProbationRecord(crn, applyDocTypeFilter);
    }

    @ApiOperation(value = "Gets the requirement data by CRN and conviction ID.")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = RequirementsResponse.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found. For example if the CRN can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path="offender/{crn}/convictions/{convictionId}/requirements", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    RequirementsResponse getRequirements(@PathVariable String crn, @PathVariable String convictionId) {
        return new RequirementsResponse(offenderService.getConvictionRequirements(crn, convictionId));
    }

    @ApiOperation(value = "Gets a document by ID for a CRN.")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = HttpEntity.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found. If the CRN can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path="offender/{crn}/documents/{documentId}")
    public HttpEntity<Resource> getOffenderDocumentByCrn(
        @ApiParam(name = "crn", value = "CRN for the offender", example = "X320741", required = true) @NotNull final @PathVariable("crn") String crn,
        @ApiParam(name = "documentId", value = "Document Id", example = "12312322", required = true) @NotNull final @PathVariable("documentId") String documentId) {

        return Optional.ofNullable(documentService.getDocument(crn, documentId))
            .orElse(new ResponseEntity<>(NOT_FOUND));
    }
}
