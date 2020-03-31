package uk.gov.justice.probation.courtcaseservice.controller;

import static org.springframework.http.HttpStatus.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Api(tags = "Court Resources")
@RestController
@Slf4j
public class CourtController {

    private final CourtService courtService;

    @Autowired
    public CourtController(CourtService courtService) {
        this.courtService = courtService;
    }

    @ApiOperation(value = "Creates the court entity data. Will not overwrite a record with the same court code.")
    @ApiResponses(
        value = {
            @ApiResponse(code = 201, message = "Created", response = CourtEntity.class),
            @ApiResponse(code = 400, message = "Invalid request. For example, court code parameter does not match one in RequestBody"
                + "or court code already exists.",
                response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @PutMapping(value = "/court/{courtCode}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(CREATED)
    public @ResponseBody
    CourtEntity createCourt(@PathVariable(value = "courtCode") @NotEmpty String courtCode, @Valid @RequestBody CourtEntity courtEntity) {
        if (!courtCode.equals(courtEntity.getCourtCode())) {
            throw new ConflictingInputException(String.format("Court code in path '%s' does not match court code in body '%s'", courtCode, courtEntity.getCourtCode()));
        }
        return courtService.updateCourt(courtEntity);
    }

}
