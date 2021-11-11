package uk.gov.justice.probation.courtcaseservice.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.mapper.CourtCaseResponseMapper;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseListResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.ExtendedCourtCaseRequestResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.time.LocalTime.MIDNIGHT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api(tags = "Court and Cases Resources")
@RestController
@AllArgsConstructor
public class CourtCaseController {

    // See https://www.postgresql.org/docs/9.0/datatype-datetime.html
    private static final int MAX_YEAR_SUPPORTED_BY_DB = 294276;
    private static final int MAX_AGE = 1;
    private static final LocalDateTime NEVER_MODIFIED_DATE = LocalDateTime.of(2020, MAX_AGE, MAX_AGE, 0, 0);
    private final CourtCaseService courtCaseService;
    private final OffenderMatchService offenderMatchService;

    @ApiOperation(value = "Gets the court case data by case id.")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found. For example if the court code or case number can't be matched.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(value = "/case/{caseId}/defendant/{defendantId}", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    CourtCaseResponse getCourtCaseByCaseIdAndDefendantId(@PathVariable String caseId, @PathVariable String defendantId) {
        return this.buildCourtCaseResponseForCaseIdAndDefendantId(courtCaseService.getCaseByCaseIdAndDefendantId(caseId, defendantId), defendantId);
    }

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
        return buildCourtCaseResponse(courtCaseService.getCaseByCaseNumber(courtCode, caseNo));
    }

    @ApiOperation(value = "Saves and returns the court case data, by case id.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 201, message = "Created", response = ExtendedCourtCaseRequestResponse.class),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not Found, if for example, the court code does not exist.", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @PutMapping(value = "/case/{caseId}/extended", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    Mono<ExtendedCourtCaseRequestResponse> updateCourtCaseId(@PathVariable(value = "caseId") String caseId,
                                                             @Valid @RequestBody ExtendedCourtCaseRequestResponse courtCaseRequest) {
        return courtCaseService.createCase(caseId, courtCaseRequest.asCourtCaseEntity())
            .thenReturn(courtCaseRequest);
    }

    @ApiOperation(value = "Returns extended court case data, by case id.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = ExtendedCourtCaseRequestResponse.class),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not Found, if for example, the court code does not exist.", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "/case/{caseId}/extended", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    ExtendedCourtCaseRequestResponse getExtendedCourtCase(@PathVariable(value = "caseId") String caseId) {
        final var courtCase = courtCaseService.getCaseByCaseId(caseId);
        return ExtendedCourtCaseRequestResponse.of(courtCase);
    }

    @ApiOperation(value = "Saves and returns the court case data, by case id.")
    @ApiResponses(
        value = {
            @ApiResponse(code = 201, message = "Updated", response = ExtendedCourtCaseRequestResponse.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found, if for example, the court code does not exist.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @PutMapping(value = "/case/{caseId}/defendant/{defendantId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    Mono<CourtCaseResponse> updateCourtCaseByDefendantId(@PathVariable(value = "caseId") String caseId,
        @PathVariable(value = "defendantId") String defendantId,
        @Valid @RequestBody CourtCaseRequest courtCaseRequest) {
        return courtCaseService.createUpdateCaseForSingleDefendantId(caseId, defendantId, courtCaseRequest.asEntity())
            .map(courtCaseEntity -> buildCourtCaseResponseForCaseIdAndDefendantId(courtCaseEntity, defendantId));
    }

    @ApiOperation(value = "Gets case data for a court on a date. ",
            notes = "Response is sorted by court room, session start time and by defendant surname. The createdAfter and " +
                    "createdBefore filters will not filter out updates originating from prepare-a-case, these manual updates" +
                    " are always assumed to be correct as they have been deliberately made by authorised users rather than " +
                    "automated systems.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = CaseListResponse.class),
                    @ApiResponse(code = 304, message = "Not modified"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "If the court is not found by the code passed."),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "/court/{courtCode}/cases", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseListResponse> getCaseList(
            @PathVariable String courtCode,
            @RequestParam(value = "date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "createdAfter", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(value = "createdBefore", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore,
            WebRequest webRequest
    ) {
        var lastModified = courtCaseService.filterCasesLastModified(courtCode, date)
            .orElse(NEVER_MODIFIED_DATE)
            .toInstant(ZoneOffset.UTC);
        if (webRequest.checkNotModified(lastModified.toEpochMilli())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .cacheControl(CacheControl.maxAge(MAX_AGE, TimeUnit.SECONDS))
                    .build();
        }

        final var createdAfterOrDefault = Optional.ofNullable(createdAfter)
                .orElse(
                        LocalDateTime.of(date, MIDNIGHT).minusDays(8)
                );

        final var createdBeforeOrDefault = Optional.ofNullable(createdBefore)
                .orElse(LocalDateTime.of(MAX_YEAR_SUPPORTED_BY_DB, 12, 31, 23, 59));

        var courtCases = courtCaseService.filterCases(courtCode, date, createdAfterOrDefault, createdBeforeOrDefault);
        var courtCaseResponses = courtCases.stream()
                .flatMap(courtCaseEntity -> buildCourtCaseResponses(courtCaseEntity, date).stream())
                .sorted(Comparator
                        .comparing(CourtCaseResponse::getCourtRoom)
                        .thenComparing(CourtCaseResponse::getSessionStartTime)
                        .thenComparing(CourtCaseResponse::getName))
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .lastModified(lastModified)
                .cacheControl(CacheControl.maxAge(MAX_AGE, TimeUnit.SECONDS))
                .body(CaseListResponse.builder().cases(courtCaseResponses).build());
    }

    private CourtCaseResponse buildCourtCaseResponseForCaseIdAndDefendantId(CourtCaseEntity courtCaseEntity, String defendantId) {
        final var offenderMatchesCount = offenderMatchService.getMatchCountByCaseIdAndDefendant(courtCaseEntity.getCaseId(), defendantId)
            .orElse(0);

        return CourtCaseResponseMapper.mapFrom(courtCaseEntity, defendantId, offenderMatchesCount);
    }

    private CourtCaseResponse buildCourtCaseResponse(CourtCaseEntity courtCaseEntity) {
        final var defendantId = Optional.ofNullable(courtCaseEntity.getDefendants())
                .flatMap(defs -> defs.stream().findFirst())
                .map(DefendantEntity::getDefendantId)
                .orElseThrow(() -> new IllegalStateException(String.format("Court case with id %s does not have any defendants.", courtCaseEntity.getCaseId())));

        return buildCourtCaseResponseForCaseIdAndDefendantId(courtCaseEntity, defendantId);
    }

    private List<CourtCaseResponse> buildCourtCaseResponses(CourtCaseEntity courtCaseEntity, LocalDate hearingDate) {

        var defendantEntities = new ArrayList<>(Optional.ofNullable(courtCaseEntity.getDefendants()).orElse(Collections.emptyList()));

        final var caseId = courtCaseEntity.getCaseId();
        return defendantEntities.stream()
            .sorted(Comparator.comparing(DefendantEntity::getDefendantSurname))
            .map(defendantEntity ->  {
                var matchCount = offenderMatchService.getMatchCountByCaseIdAndDefendant(caseId, defendantEntity.getDefendantId()).orElse(0);
                return CourtCaseResponseMapper.mapFrom(courtCaseEntity, defendantEntity, matchCount, hearingDate);
            })
            .collect(Collectors.toList());
    }


}
