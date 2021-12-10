package uk.gov.justice.probation.courtcaseservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtListResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Court Resources")
@RestController
@Slf4j
public class CourtController {

    private final CourtService courtService;

    @Autowired
    public CourtController(CourtService courtService) {
        this.courtService = courtService;
    }

    @Operation(description = "Creates the court entity data. Will not overwrite a record with the same court code.")
//    @ApiResponses(
//        value = {
//            @ApiResponse(code = 201, message = "Created", response = CourtEntity.class),
//            @ApiResponse(code = 400, message = "Invalid request. For example, court code parameter does not match one in RequestBody"
//                + "or court code already exists.",
//                response = ErrorResponse.class),
//            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
//            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
//            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
//        })
    @PutMapping(value = "/court/{courtCode}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(CREATED)
    public @ResponseBody
    CourtEntity createCourt(@PathVariable(value = "courtCode") @NotEmpty String courtCode, @Valid @RequestBody CourtEntity courtEntity) {
        if (!courtCode.equals(courtEntity.getCourtCode())) {
            throw new ConflictingInputException(String.format("Court code in path '%s' does not match court code in body '%s'", courtCode, courtEntity.getCourtCode()));
        }
        return courtService.updateCourt(courtEntity);
    }

    @Operation(description = "Gets a list of all courts with code and names.")
//    @ApiResponses(
//        value = {
//            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
//            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
//            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
//        })
    @GetMapping(value = "/courts", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    CourtListResponse getCourts() {
        List<CourtResponse> courtResponseList = courtService.getCourts()
            .stream()
            .map(this::buildCourt)
            .collect(Collectors.toList());
        return CourtListResponse.builder().courts(courtResponseList).build();
    }

    private CourtResponse buildCourt(CourtEntity courtEntity) {
        return CourtResponse.builder()
            .name(courtEntity.getName())
            .code(courtEntity.getCourtCode())
            .build();
    }
}
