package uk.gov.justice.probation.courtcaseservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.controller.mapper.CourtCaseResponseMapper;
import uk.gov.justice.probation.courtcaseservice.controller.model.*;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.*;
import uk.gov.justice.probation.courtcaseservice.service.*;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;
import uk.gov.justice.probation.courtcaseservice.service.model.HearingSearchFilter;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
        HearingEntity hearingByHearingIdAndDefendantId = courtCaseService.getHearingByHearingIdAndDefendantIdInitialiseCaseDefendants(hearingId, defendantId);
        List<CaseProgressHearing> caseHearingProgress = caseProgressService.getCaseHearingProgress(hearingByHearingIdAndDefendantId.getCaseId(), defendantId);
        return this.buildCourtCaseResponseForCaseIdAndDefendantId(hearingByHearingIdAndDefendantId, defendantId, caseHearingProgress);
    }

    @Operation(description = "Gets the court case and hearing data by Libra caseNo. As Libra cases have no hearingId, the listNo of the Libra case is used to differentiate between hearings.")
    @GetMapping(value = "/court/{courtCode}/case/{caseNo}", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    CourtCaseResponse getCourtCase(
            @PathVariable String courtCode,
            @PathVariable String caseNo,
            @Parameter(in = ParameterIn.PATH, name = "listNo", schema = @Schema(type = "string"), description = "If listNo is provided then the endpoint will return the latest hearing with matching listNo if it exists. If the case exists, but a hearing with the provided listNo does not, then the endpoint will search and return the hearing with no listNo if present or return a HTTP 404. This indicates to the caller that they should generate a new hearingId when PUTting to create a new hearing entity. This is required to allow Libra case progress to be tracked, see <a href='https://dsdmoj.atlassian.net/browse/PIC-2293'>PIC-2293</a> for more information.")
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

    @Operation(description = "Creates a hearing note for a given hearing and defendant ID")
    @PostMapping(value = "/hearing/{hearingId}/defendants/{defendantId}/notes", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    HearingNoteResponse createHearingNote(@PathVariable(value = "hearingId") String hearingId,
                                          @PathVariable(value = "defendantId") String defendantId,
                                          @Valid @RequestBody HearingNoteRequest hearingNoteRequest,
                                          Principal principal) {

        HearingNoteEntity hearingNote = hearingNotesService.createHearingNote(hearingId, defendantId, hearingNoteRequest.asEntity(authenticationHelper.getAuthUserUuid(principal)));
        return HearingNoteResponse.of(hearingNote);
    }

    @Operation(description = "Creates/updates a draft hearing note for a given hearing and defendant ID")
    @PutMapping(value = "/hearing/{hearingId}/defendants/{defendantId}/notes/draft", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    HearingNoteResponse createUpdateDraftHearingNote(@PathVariable(value = "hearingId") String hearingId,
                                                     @PathVariable(value = "defendantId") String defendantId,
                                          @Valid @RequestBody HearingNoteRequest hearingNoteRequest,
                                          Principal principal) {

        HearingNoteEntity hearingNote = hearingNotesService
            .createOrUpdateHearingNoteDraft(hearingId, defendantId, hearingNoteRequest.asEntity(authenticationHelper.getAuthUserUuid(principal)));
        return HearingNoteResponse.of(hearingNote);
    }

    @Operation(description = "Deletes the draft hearing note for a given hearing")
    @DeleteMapping(value = "/hearing/{hearingId}/defendants/{defendantId}/notes/draft")
    @ResponseStatus(HttpStatus.OK)
    public void deleteDraftHearingNote(@PathVariable(value = "hearingId") String hearingId,
                                       @PathVariable(value = "defendantId") String defendantId,
                                               Principal principal) {

        hearingNotesService
            .deleteHearingNoteDraft(hearingId, defendantId, authenticationHelper.getAuthUserUuid(principal));
    }

    @Operation(description = "Updates a hearing note for a given hearing, defendant and noteId")
    @PutMapping(value = "/hearing/{hearingId}/defendants/{defendantId}/notes/{noteId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void updateHearingNote(@PathVariable(value = "hearingId") String hearingId,
                                  @PathVariable(value = "defendantId") String defendantId,
                                  @PathVariable(value = "noteId") Long noteId,
                                  @Valid @RequestBody HearingNoteRequest hearingNoteRequest,
                                          Principal principal) {

        hearingNotesService.updateHearingNote(hearingId, defendantId, hearingNoteRequest.asEntity(authenticationHelper.getAuthUserUuid(principal)), noteId);
    }

    @Operation(description = "Delete a hearing note for a given hearing and note id")
    @DeleteMapping(value = "/hearing/{hearingId}/defendants/{defendantId}/notes/{noteId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteHearingNote(@PathVariable(value = "hearingId") String hearingId,
                                  @PathVariable(value = "defendantId") String defendantId,
                                  @PathVariable(value = "noteId") Long noteId,
                                  Principal principal) {

        hearingNotesService.deleteHearingNote(hearingId, defendantId, noteId, authenticationHelper.getAuthUserUuid(principal));
    }


    @Operation(description = "Creates/updates a draft case comment for a given case")
    @PutMapping(value = "/cases/{caseId}/defendants/{defendantId}/comments/draft", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    CaseCommentResponse createUpdateCaseCommentDraft(@PathVariable(value = "caseId") String caseId,
                                                     @PathVariable(value = "defendantId") String defendantId,
                                                     @Valid @RequestBody CaseCommentRequest caseCommentRequest,
                                                     Principal principal) {

        var caseCommentEntity = caseCommentsService.createUpdateCaseCommentDraft(caseCommentRequest.asEntity(authenticationHelper.getAuthUserUuid(principal), caseId, defendantId));
        return CaseCommentResponse.of(caseCommentEntity);
    }

    @Operation(description = "Updates a case comment for a given case id and comment id")
    @PutMapping(value = "/cases/{caseId}/defendants/{defendantId}/comments/{commentId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    CaseCommentResponse updateCaseComment(@PathVariable(value = "caseId") String caseId,
                                          @PathVariable(value = "defendantId") String defendantId,
                                          @PathVariable(value = "commentId") Long commentId,
                                          @Valid @RequestBody CaseCommentRequest caseCommentRequest,
                                          Principal principal) {

        validateCaseCommentRequest(caseId, caseCommentRequest);
        var caseCommentEntity = caseCommentsService.updateCaseComment(
            caseCommentRequest.asEntity(authenticationHelper.getAuthUserUuid(principal), caseId, defendantId), commentId);
        return CaseCommentResponse.of(caseCommentEntity);
    }

    @Operation(description = "Deletes a draft case comment for a given case")
    @DeleteMapping(value = "/cases/{caseId}/defendants/{defendantId}/comments/draft")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    void deleteCaseCommentDraft(@PathVariable(value = "caseId") String caseId,
                                @PathVariable(value = "defendantId") String defendantId, Principal principal) {
        caseCommentsService.deleteCaseCommentDraft(caseId, defendantId, authenticationHelper.getAuthUserUuid(principal));
    }

    @Operation(description = "Creates a comment on given court case.")
    @PostMapping(value = "/cases/{caseId}/defendants/{defendantId}/comments", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    CaseCommentResponse createCaseComment(@PathVariable(value = "caseId") String caseId,
                                          @PathVariable(value = "defendantId") String defendantId,
                                          @RequestBody CaseCommentRequest caseCommentRequest,
                                          Principal principal) {

        var caseCommentEntity = caseCommentsService.createCaseComment(
            CaseCommentEntity.builder().caseId(caseId).defendantId(defendantId)
            .createdByUuid(authenticationHelper.getAuthUserUuid(principal))
                .author(caseCommentRequest.getAuthor())
                .comment(caseCommentRequest.getComment())
                .build());
        return CaseCommentResponse.of(caseCommentEntity);
    }

    private static void validateCaseCommentRequest(String caseId, CaseCommentRequest caseCommentRequest) {
        if (!StringUtils.equals(caseId, caseCommentRequest.getCaseId())) {
            throw new ConflictingInputException(String.format("Case Id '%s' provided in the path does not match the one in the case comment request body submitted '%s'",
                caseId, caseCommentRequest.getCaseId()));
        }
    }

    @Operation(description = "Deletes a comment from a given court case.")
    @DeleteMapping(value = "/cases/{caseId}/defendants/{defendantId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    void deleteCaseComment(@PathVariable(value = "caseId") String caseId,
                           @PathVariable(value = "defendantId") String defendantId,
                           @PathVariable(value = "commentId") Long commentId,
                           Principal principal) {
        caseCommentsService.deleteCaseComment(caseId, defendantId, commentId, authenticationHelper.getAuthUserUuid(principal));
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
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "breach", defaultValue = "false") boolean breach,
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


        final var hearingSearchFilter = HearingSearchFilter.builder()
                .courtCode(courtCode)
                .hearingDay(date)
                .source(source)
                .breach(breach)
                .build();

        var courtCases = courtCaseService.filterHearings(hearingSearchFilter);

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

    @Operation(summary = "Gets case data for a court on a date and parameters",
            description = "Response is sorted by court room, session start time and by defendant surname. The createdAfter and " +
                    "createdBefore filters will not filter out updates originating from prepare-a-case, these manual updates" +
                    " are always assumed to be correct as they have been deliberately made by authorised users rather than " +
                    "automated systems.")
    @GetMapping(value = "/court/{courtCode}/matcher-cases", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseListResponse> getCaseListForMatcher(
            @PathVariable String courtCode,
            @RequestParam(value = "date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "numberOfPossibleMatches", required = false) Long numberOfPossibleMatches,
            @RequestParam(value = "forename", required = false) String forename,
            @RequestParam(value = "surname", required = false) String surname,
            @RequestParam(value = "defendantName", required = false) String defendantName,
            @RequestParam(value = "caseId", required = false) String caseId,
            @RequestParam(value = "hearingId", required = false) String hearingId,
            @RequestParam(value = "defendantId", required = false) String defendantId,
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

        final var hearingSearchFilter = HearingSearchFilter.builder()
                .courtCode(courtCode)
                .hearingDay(date)
                .source(null)
                .breach(false)
                .build();

        var courtCases = courtCaseService.filterHearings(hearingSearchFilter);

        var courtCaseResponses = filterCourtCaseResponses(date, numberOfPossibleMatches, forename, surname, defendantName, caseId, hearingId, defendantId, courtCases);

        return partialResponse.body(CaseListResponse.builder().cases(courtCaseResponses).build());
    }

    @Operation(summary = "Gets case data for a court on a date with pagination support",
        description = "Response is sorted by court room, session start time and by defendant surname. Supports filtering by Supports pagination of results.")
    @GetMapping(value = "/court/{courtCode}/cases", produces = APPLICATION_JSON_VALUE, params = { "VERSION2" })
    public CaseListResponse getCaseList(
        @PathVariable String courtCode,
        @Valid HearingSearchRequest hearingSearchRequest
    ) {
        return courtCaseService.filterHearings(courtCode, hearingSearchRequest);
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

        return defendantEntities.stream()
                .sorted(Comparator.comparing(HearingDefendantEntity::getDefendantSurname))
                .map(hearingDefendantEntity -> buildCourtCaseResponse(hearingEntity, hearingDate, hearingDefendantEntity))
                .toList();
    }

    private CourtCaseResponse buildCourtCaseResponse(HearingEntity hearingEntity, LocalDate hearingDate, HearingDefendantEntity hearingDefendantEntity) {
        final var defendant = Optional.ofNullable(hearingDefendantEntity).map(HearingDefendantEntity::getDefendant).orElseThrow();
        var matchCount = offenderMatchService.getMatchCountByCaseIdAndDefendant(hearingEntity.getCaseId(), defendant.getDefendantId()).orElse(0);
        return CourtCaseResponseMapper.mapFrom(hearingEntity, hearingDefendantEntity, matchCount, hearingDate);
    }

    List<CourtCaseResponse> filterCourtCaseResponses(LocalDate date, Long numberOfPossibleMatches, String forename, String surname,
                                                     String defendantName, String caseId, String hearingId, String defendantId, List<HearingEntity> courtCases) {
        return courtCases.stream()
                .flatMap(courtCaseEntity -> buildCourtCaseResponses(courtCaseEntity, date).stream())
                .filter(courtCaseResponse -> courtCaseResponse.getProbationStatus().equalsIgnoreCase(CourtCaseResponse.POSSIBLE_NDELIUS_RECORD_PROBATION_STATUS))
                .filter(courtCaseResponse -> numberOfPossibleMatches == null || courtCaseResponse.getNumberOfPossibleMatches() == numberOfPossibleMatches)
                .filter(courtCaseResponse -> defendantName == null || courtCaseResponse.getDefendantName().equalsIgnoreCase(defendantName))
                .filter(courtCaseResponse -> surname == null || courtCaseResponse.getDefendantSurname().equalsIgnoreCase(surname))
                .filter(courtCaseResponse -> forename == null || courtCaseResponse.getDefendantForename().equalsIgnoreCase(forename))
                .filter(courtCaseResponse -> caseId == null || courtCaseResponse.getCaseId().equalsIgnoreCase(caseId))
                .filter(courtCaseResponse -> hearingId == null || courtCaseResponse.getHearingId().equalsIgnoreCase(hearingId))
                .filter(courtCaseResponse -> defendantId == null ||courtCaseResponse.getDefendantId().equalsIgnoreCase(defendantId))
                .sorted(Comparator
                        .comparing(CourtCaseResponse::getCourtRoom)
                        .thenComparing(CourtCaseResponse::getSessionStartTime)
                        .thenComparing(CourtCaseResponse::getName))
                .collect(Collectors.toList());
    }
}
