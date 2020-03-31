package uk.gov.justice.probation.courtcaseservice.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

@Api(tags = "Offender Resources", produces = APPLICATION_JSON_VALUE)
@RestController
@AllArgsConstructor
public class OffenderController {

    @Autowired
    private OffenderService offenderService;

    @ApiOperation(value = "Gets the offender data by CRN")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = CourtEntity.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found. For example if the CRN can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path="offender/{crn}/probation-record", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody Offender getOffender(@PathVariable String crn) {
        return offenderService.getOffender(crn);
    }

    @ApiOperation(value = "Gets the requirement data by CRN and conviction ID.")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = CourtEntity.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found. For example if the CRN can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path="offender/{crn}/convictions/{convictionId}/requirements", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    Mono<List<Requirement>> getRequirements(@PathVariable String crn, @PathVariable String convictionId) {
        return offenderService.getConvictionRequirements(crn, convictionId);
    }
}
