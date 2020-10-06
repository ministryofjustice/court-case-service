package uk.gov.justice.probation.courtcaseservice.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.probation.courtcaseservice.controller.mapper.CourtCaseResponseMapper;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseListResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api(tags = "Court and Cases Resources")
@RestController
@AllArgsConstructor
public class CourtCaseController {

    private final CourtCaseService courtCaseService;
    private final CourtCaseResponseMapper courtCaseResponseMapper;

    @ApiOperation(value = "Gets the court case data by case number.")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = CourtCaseResponse.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found. For example if the court code or case number can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(value = "/court/{courtCode}/case/{caseNo}", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    CourtCaseResponse getCourtCase(@PathVariable String courtCode, @PathVariable String caseNo) {
        return courtCaseResponseMapper.mapFrom(courtCaseService.getCaseByCaseNumber(courtCode, caseNo));
    }

    @ApiOperation(value = "Saves and returns the court case entity data, by court and case number. ")
    @ApiResponses(
        value = {
            @ApiResponse(code = 201, message = "Created", response = CourtCaseResponse.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found, if for example, the court code does not exist.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @PutMapping(value = "/court/{courtCode}/case/{caseNo}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    CourtCaseResponse updateCourtCaseNo(@PathVariable(value = "courtCode") String courtCode,
                                        @PathVariable(value = "caseNo") String caseNo ,
                                        @Valid @RequestBody CourtCaseRequest courtCaseRequest) {
        return courtCaseResponseMapper.mapFrom(courtCaseService.createCase(courtCode, caseNo, courtCaseRequest.asEntity()));
    }

    @ApiOperation(value = "Gets case data for a court on a date. ",
        notes = "Response is sorted by court room, session start time and by defendant surname")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = CaseListResponse.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "If the court is not found by the code passed."),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(value = "/court/{courtCode}/cases", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    CaseListResponse getCaseList(@PathVariable String courtCode,
                                 @RequestParam("date")
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<CourtCaseEntity> courtCases = courtCaseService.filterCasesByCourtAndDate(courtCode, date);
        List<CourtCaseResponse> courtCaseResponses = courtCases.stream()
                .sorted(Comparator.comparing(CourtCaseEntity::getCourtRoom)
                                .thenComparing(CourtCaseEntity::getSessionStartTime)
                                .thenComparing(CourtCaseEntity::getDefendantSurname))
                .map(courtCaseResponseMapper::mapFrom)
                .collect(Collectors.toList());

        return CaseListResponse.builder().cases(courtCaseResponses).build();
    }
}
