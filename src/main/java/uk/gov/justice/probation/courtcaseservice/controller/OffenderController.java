package uk.gov.justice.probation.courtcaseservice.controller;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.controller.model.ConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.RequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.service.ConvictionService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api(tags = "Offender Resources", produces = APPLICATION_JSON_VALUE)
@RestController
@AllArgsConstructor
public class OffenderController {

    @Autowired
    private OffenderService offenderService;

    @Autowired
    private final ConvictionService convictionService;

    @Autowired
    private final FeatureFlags featureFlags;

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

    @GetMapping(value = "/offenders/{crn}/convictions/{convictionId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Return the conviction detail for attendances and Unpaid Work for a CRN and a conviction id where enforcement is flagged")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = ConvictionResponse.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not found. For example if the CRN can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    public ConvictionResponse getConviction(@PathVariable String crn, @PathVariable Long convictionId) {
        if (!featureFlags.attendanceData()) {
            return convictionService.getConvictionNoAttendances(crn, convictionId);
        }
        return convictionService.getConviction(crn, convictionId);
    }
}
