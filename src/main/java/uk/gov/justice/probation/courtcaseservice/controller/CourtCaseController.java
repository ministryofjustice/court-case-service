package uk.gov.justice.probation.courtcaseservice.controller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.controller.mapper.CourtCaseResponseMapper;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseCommentRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseCommentResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseListResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.DefendantOffender;
import uk.gov.justice.probation.courtcaseservice.controller.model.ExtendedHearingRequestResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNoteRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNoteResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;
import uk.gov.justice.probation.courtcaseservice.service.AuthenticationHelper;
import uk.gov.justice.probation.courtcaseservice.service.CaseCommentsService;
import uk.gov.justice.probation.courtcaseservice.service.CaseProgressService;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;
import uk.gov.justice.probation.courtcaseservice.service.HearingNotesService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderUpdateService;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;

import javax.validation.Valid;
import java.security.Principal;
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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@OpenAPIDefinition(info =
@Info(
        title = "court-case-service",
        description = "Service to access court cases imported from HMCTS Libra and Common Platform court lists",
        license = @License(name = "The MIT License (MIT)", url = "https://github.com/ministryofjustice/court-case-service/blob/main/LICENSE")
)
)
@Tag(name = "Court Case Resources")
@RestController
public class CourtCaseController {

    // See https://www.postgresql.org/docs/9.0/datatype-datetime.html
    private static final int MAX_YEAR_SUPPORTED_BY_DB = 294276;
    private static final int MIN_YEAR_SUPPORTED_BY_DB = -4712;
    private static final int MAX_AGE = 1;
    private static final LocalDateTime NEVER_MODIFIED_DATE = LocalDateTime.of(2020, MAX_AGE, MAX_AGE, 0, 0);
    private final CourtCaseService courtCaseService;
    private final OffenderMatchService offenderMatchService;
    private final OffenderUpdateService offenderUpdateService;
    private final boolean enableCacheableCaseList;
    private final CaseCommentsService caseCommentsService;
    private final AuthenticationHelper authenticationHelper;
    private final CaseProgressService caseProgressService;
    private final HearingNotesService hearingNotesService;

    @Autowired
    public CourtCaseController(CourtCaseService courtCaseService,
                               OffenderMatchService offenderMatchService,
                               OffenderUpdateService offenderUpdateService,
                               CaseCommentsService caseCommentsService,
                               AuthenticationHelper authenticationHelper,
                               CaseProgressService caseProgressService,
                               HearingNotesService hearingNotesService,
                               @Value("${feature.flags.enable-cacheable-case-list:true}") boolean enableCacheableCaseList) {
        this.courtCaseService = courtCaseService;
        this.offenderMatchService = offenderMatchService;
        this.offenderUpdateService = offenderUpdateService;
        this.enableCacheableCaseList = enableCacheableCaseList;
        this.caseCommentsService = caseCommentsService;
        this.authenticationHelper = authenticationHelper;
        this.caseProgressService = caseProgressService;
        this.hearingNotesService = hearingNotesService;
    }

    @Operation(description = "Gets the court case data by hearing id and defendant id.")
    @GetMapping(value = "/hearing/{hearingId}/defendant/{defendantId}", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    CourtCaseResponse getCourtCaseByHearingIdAndDefendantId(@PathVariable String hearingId, @PathVariable String defendantId) {
        HearingEntity hearingByHearingIdAndDefendantId = courtCaseService.getHearingByHearingIdAndDefendantId(hearingId, defendantId);
        List<CaseProgressHearing> caseHearingProgress = caseProgressService.getCaseHearingProgress(hearingByHearingIdAndDefendantId.getCaseId());
        return this.buildCourtCaseResponseForCaseIdAndDefendantId(hearingByHearingIdAndDefendantId, defendantId, caseHearingProgress);
    }

    @Operation(description = "Gets the court case and hearing data by Libra caseNo. As Libra cases have no hearingId, the listNo of the Libra case is used to differentiate between hearings.")
    @GetMapping(value = "/court/{courtCode}/case/{caseNo}", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    CourtCaseResponse getCourtCase(
            @PathVariable String courtCode,
            @PathVariable String caseNo,
            @Parameter(in = ParameterIn.PATH, name = "listNo", schema = @Schema(type = "string"), description = "If listNo is provided then the endpoint will return the latest hearing with matching listNo if it exists. If the case exists, but a hearing with the provided listNo does not, then the endpoint will return the most recent hearing but will <em>omit</em> the hearingId. This indicates to the caller that they should generate a new hearingId when PUTting to create a new hearing entity. This is required to allow Libra case progress to be tracked, see <a href='https://dsdmoj.atlassian.net/browse/PIC-2293'>PIC-2293</a> for more information.")
            @RequestParam(required = false) String listNo) {
        return buildCourtCaseResponse(courtCaseService.getHearingByCaseNumber(courtCode, caseNo, listNo));
    }

    @Operation(description = "Saves and returns the court case data, by hearing id.")
    @PutMapping(value = "/hearing/{hearingId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    Mono<ExtendedHearingRequestResponse> createOrUpdateHearingByHearingId(@PathVariable(value = "hearingId") String hearingId,
                                                                          @Valid @RequestBody ExtendedHearingRequestResponse putHearingRequest) {
        return courtCaseService.createOrUpdateHearingByHearingId(hearingId, putHearingRequest.asHearingEntity())
                .map(ExtendedHearingRequestResponse::of);
    }

    @Operation(description = "Creates a hearing note for a given hearing")
    @PostMapping(value = "/hearing/{hearingId}/notes", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    HearingNoteResponse createHearingNote(@PathVariable(value = "hearingId") String hearingId,
                                          @Valid @RequestBody HearingNoteRequest hearingNoteRequest,
                                          Principal principal) {

        if (!StringUtils.equals(hearingId, hearingNoteRequest.getHearingId())) {
            throw new ConflictingInputException(String.format("Hearing Id '%s' provided in the path does not match the one in the hearing note request body submitted '%s'",
                    hearingId, hearingNoteRequest.getHearingId()));
        }

        HearingNoteEntity hearingNote = hearingNotesService.createHearingNote(hearingNoteRequest.asEntity(authenticationHelper.getAuthUserUuid(principal)));
        return HearingNoteResponse.of(hearingNote);
    }

    @Operation(description = "Delete a hearing note for a given hearing and note id")
    @DeleteMapping(value = "/hearing/{hearingId}/notes/{noteId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteHearingNote(@PathVariable(value = "hearingId") String hearingId,
                                  @PathVariable(value = "noteId") Long noteId,
                                  Principal principal) {

        hearingNotesService.deleteHearingNote(hearingId, noteId, authenticationHelper.getAuthUserUuid(principal));
    }

    @Operation(description = "Creates a comment on given court case.")
    @PostMapping(value = "/cases/{caseId}/comments", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    CaseCommentResponse createCaseComment(@PathVariable(value = "caseId") String caseId,
                                          @RequestBody CaseCommentRequest caseCommentRequest,
                                          Principal principal) {

        if (!StringUtils.equals(caseId, caseCommentRequest.getCaseId())) {
            throw new ConflictingInputException(String.format("Case Id '%s' provided in the path does not match the one in the case comment request body submitted '%s'",
                    caseId, caseCommentRequest.getCaseId()));
        }
        var caseCommentEntity = caseCommentsService.createCaseComment(caseCommentRequest.asEntity(authenticationHelper.getAuthUserUuid(principal)));
        return CaseCommentResponse.of(caseCommentEntity);
    }

    @Operation(description = "Deletes a comment from a given court case.")
    @DeleteMapping(value = "/cases/{caseId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    void deleteCaseComment(@PathVariable(value = "caseId") String caseId,
                           @PathVariable(value = "commentId") Long commentId,
                           Principal principal) {
        caseCommentsService.deleteCaseComment(caseId, commentId, authenticationHelper.getAuthUserUuid(principal));
    }

    @Operation(description = "Returns extended court case data, by hearing id.")
    @GetMapping(value = "/hearing/{hearingId}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    ExtendedHearingRequestResponse getHearingByHearingId(@PathVariable(value = "hearingId") String hearingId) {
        final var hearingEntity = courtCaseService.getHearingByHearingId(hearingId);
        return ExtendedHearingRequestResponse.of(hearingEntity);
    }

    @Operation(description = "Saves and returns the offender details by defendant id.")
    @PutMapping(value = "/defendant/{defendantId}/offender", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<DefendantOffender> updateOffenderByDefendantId(@PathVariable(value = "defendantId") String defendantId,
                                                        @Valid @RequestBody DefendantOffender defendantOffender) {
        return offenderUpdateService.updateDefendantOffender(defendantId, defendantOffender.asEntity()).map(DefendantOffender::of);
    }

    @Operation(description = "Removes defendant offender association by defendant id.")
    @DeleteMapping(value = "/defendant/{defendantId}/offender", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteOffender(@PathVariable(value = "defendantId") String defendantId) {
        offenderUpdateService.removeDefendantOffenderAssociation(defendantId);
    }

    @Operation(description = "Returns the offender details by defendant id.")
    @GetMapping(value = "/defendant/{defendantId}/offender", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<DefendantOffender> getOffenderByDefendantId(@PathVariable(value = "defendantId") String defendantId) {
        return offenderUpdateService.getDefendantOffenderByDefendantId(defendantId).map(DefendantOffender::of);
    }

    @Operation(summary = "Gets case data for a court on a date. ",
            description = "Response is sorted by court room, session start time and by defendant surname. The createdAfter and " +
                    "createdBefore filters will not filter out updates originating from prepare-a-case, these manual updates" +
                    " are always assumed to be correct as they have been deliberately made by authorised users rather than " +
                    "automated systems.")
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
        var partialResponse = ResponseEntity.ok();
        if (enableCacheableCaseList) {
            var lastModified = courtCaseService.filterHearingsLastModified(courtCode, date)
                    .orElse(NEVER_MODIFIED_DATE)
                    .toInstant(ZoneOffset.UTC);
            if (webRequest.checkNotModified(lastModified.toEpochMilli())) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                        .cacheControl(CacheControl.maxAge(MAX_AGE, TimeUnit.SECONDS))
                        .build();
            }

            partialResponse = partialResponse
                    .lastModified(lastModified)
                    .cacheControl(CacheControl.maxAge(MAX_AGE, TimeUnit.SECONDS));
        }

        final var createdAfterOrDefault = Optional.ofNullable(createdAfter)
                .orElse(
                        LocalDateTime.of(MIN_YEAR_SUPPORTED_BY_DB, 1, 1, 0, 0)
                );

        final var createdBeforeOrDefault = Optional.ofNullable(createdBefore)
                .orElse(LocalDateTime.of(MAX_YEAR_SUPPORTED_BY_DB, 12, 31, 23, 59));

        var courtCases = courtCaseService.filterHearings(courtCode, date, createdAfterOrDefault, createdBeforeOrDefault);
        var courtCaseResponses = courtCases.stream()
                .flatMap(courtCaseEntity -> buildCourtCaseResponses(courtCaseEntity, date).stream())
                .sorted(Comparator
                        .comparing(CourtCaseResponse::getCourtRoom)
                        .thenComparing(CourtCaseResponse::getSessionStartTime)
                        .thenComparing(CourtCaseResponse::getName))
                .collect(Collectors.toList());

        return partialResponse
                .body(CaseListResponse.builder().cases(courtCaseResponses).build());
    }

    private CourtCaseResponse buildCourtCaseResponseForCaseIdAndDefendantId(HearingEntity hearingEntity, String defendantId, List<CaseProgressHearing> caseHearings) {
        final var offenderMatchesCount = offenderMatchService.getMatchCountByCaseIdAndDefendant(hearingEntity.getCaseId(), defendantId)
                .orElse(0);
        return CourtCaseResponseMapper.mapFrom(hearingEntity, defendantId, offenderMatchesCount, caseHearings);
    }

    private CourtCaseResponse buildCourtCaseResponse(HearingEntity hearingEntity) {
        final var defendantId = Optional.ofNullable(hearingEntity.getHearingDefendants())
                .flatMap(defs -> defs.stream().findFirst())
                .map(HearingDefendantEntity::getDefendant)
                .map(DefendantEntity::getDefendantId)
                .orElseThrow(() -> new IllegalStateException(String.format("Court case with id %s does not have any defendants.", hearingEntity.getCaseId())));

        return buildCourtCaseResponseForCaseIdAndDefendantId(hearingEntity, defendantId, null);
    }

    private List<CourtCaseResponse> buildCourtCaseResponses(HearingEntity hearingEntity, LocalDate hearingDate) {

        var defendantEntities = new ArrayList<>(Optional.ofNullable(hearingEntity.getHearingDefendants()).orElse(Collections.emptyList()));

        final var caseId = hearingEntity.getCaseId();
        return defendantEntities.stream()
                .sorted(Comparator.comparing(HearingDefendantEntity::getDefendantSurname))
                .map(hearingDefendantEntity -> {
                    final String defendantId = Optional.ofNullable(hearingDefendantEntity).map(HearingDefendantEntity::getDefendant).map(DefendantEntity::getDefendantId).orElseThrow();
                    var matchCount = offenderMatchService.getMatchCountByCaseIdAndDefendant(caseId, defendantId).orElse(0);
                    return CourtCaseResponseMapper.mapFrom(hearingEntity, hearingDefendantEntity, matchCount, hearingDate);
                })
                .toList();
    }
}
