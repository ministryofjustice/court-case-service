package uk.gov.justice.probation.courtcaseservice.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetailResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api(tags = "Offender Matches Resources")
@RestController
@AllArgsConstructor
public class OffenderMatchesController {
    private final OffenderMatchService offenderMatchService;

    // TODO: Delete me
    @ApiOperation(value = "Creates a new offender-match entity associated with a case")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = GroupedOffenderMatchesEntity.class),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not Found, if for example, the court code does not exist.", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @PostMapping(value = "/court/{courtCode}/case/{caseNo}/grouped-offender-matches", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    Mono<ResponseEntity<Object>> createGroupedOffenderMatches(@PathVariable(value = "courtCode") String courtCode,
                                       @PathVariable(value = "caseNo") String caseNo,
                                       @Valid @RequestBody GroupedOffenderMatchesRequest request) {
        return offenderMatchService.createOrUpdateGroupedMatches(courtCode, caseNo, request)
            .map(match -> ResponseEntity.created(URI.create(String.format("/court/%s/case/%s/grouped-offender-matches/%s", courtCode, caseNo, match.getId())))
                    .build());
    }

    @ApiOperation(value = "Creates a new offender-match entity associated with a case and a defendant ID")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = GroupedOffenderMatchesEntity.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found, if for example, the court code does not exist.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @PostMapping(value = "/case/{caseId}/defendant/{defendantId}/grouped-offender-matches", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    Mono<ResponseEntity<Object>> createGroupedOffenderMatchesByDefendant(@PathVariable(value = "caseId") String caseId,
        @PathVariable(value = "defendantId") String defendantId,
        @Valid @RequestBody GroupedOffenderMatchesRequest request) {
        return offenderMatchService.createOrUpdateGroupedMatchesByDefendant(caseId, defendantId, request)
            .map(match -> ResponseEntity.created(URI.create(String.format("/case/%s/defendant/%s/grouped-offender-matches/%s", caseId, defendantId, match.getId())))
                .build());
    }

    // TODO: Delete me
    @ApiOperation(value = "Gets an existing offender-match entity associated with a case")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = GroupedOffenderMatchesEntity.class),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not Found, if for example, the court code does not exist.", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "/court/{courtCode}/case/{caseNo}/grouped-offender-matches/{groupId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<GroupedOffenderMatchesEntity> getOffenderMatches(@PathVariable(value = "courtCode") String courtCode,
                                                          @PathVariable(value = "caseNo") String caseNo,
                                                          @PathVariable(value = "groupId") Long groupId) {
         return offenderMatchService.getGroupedMatches(courtCode, caseNo, groupId);
    }

    @ApiOperation(value = "Gets an existing offender-match entity associated with a case")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = GroupedOffenderMatchesEntity.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found, if for example, the court code does not exist.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(value = "/case/{caseId}/defendant/{defendantId}/grouped-offender-matches/{groupId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<GroupedOffenderMatchesEntity> getOffenderMatchesByCaseId(@PathVariable(value = "caseId") String caseId,
        @PathVariable(value = "defendantId") String defendantId,
        @PathVariable(value = "groupId") Long groupId) {
        return offenderMatchService.getGroupedMatchesByCaseId(caseId, defendantId, groupId);
    }

    // TODO: Delete me
    @ApiOperation(value = "Returns all possible matches found for a given case")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found, if for example, the court code does not exist or the case for a court.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(value = "/court/{courtCode}/case/{caseNo}/matchesDetail", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    OffenderMatchDetailResponse getOffenderMatchesDetail(@PathVariable(value = "courtCode") String courtCode,
                                                        @PathVariable(value = "caseNo") String caseNo) {
        return offenderMatchService.getOffenderMatchDetails(courtCode, caseNo);
    }

    @ApiOperation(value = "Returns all possible matches found for a given case and defendant ID")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found, if for example, the case or defendant IDs do not exist.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(value = "/case/{caseId}/defendant/{defendantId}/matchesDetail", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    OffenderMatchDetailResponse getOffenderMatchesDetailByCaseAndDefendantId(@PathVariable(value = "caseId") String caseId,
                                                                            @PathVariable(value = "defendantId") String defendantId) {
        return offenderMatchService.getOffenderMatchDetailsByCaseIdAndDefendantId(caseId, defendantId);
    }
}
