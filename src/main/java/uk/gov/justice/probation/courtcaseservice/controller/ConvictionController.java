package uk.gov.justice.probation.courtcaseservice.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendancesResponse;
import uk.gov.justice.probation.courtcaseservice.service.ConvictionService;

@RestController
@Slf4j
public class ConvictionController {

    private final ConvictionService convictionService;

    private final FeatureFlags featureFlags;

    @Autowired
    public ConvictionController(final ConvictionService service, final FeatureFlags decisions) {
        this.convictionService = service;
        this.featureFlags = decisions;
    }

    @GetMapping(value = "/offenders/{crn}/convictions/{convictionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Return the attendances for a CRN and a conviction id where enforcement is flagged")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = AttendancesResponse.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    public AttendancesResponse getAttendances(@PathVariable String crn, @PathVariable Long convictionId) {
        if (!featureFlags.attendanceData()) {
            return new AttendancesResponse(crn, convictionId, Collections.emptyList());
        }
        return convictionService.getAttendances(crn, convictionId);
    }

}
