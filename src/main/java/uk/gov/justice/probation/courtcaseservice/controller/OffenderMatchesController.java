package uk.gov.justice.probation.courtcaseservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Offender Matches Resources")
@RestController
@AllArgsConstructor
public class OffenderMatchesController {
    private final OffenderMatchService offenderMatchService;

    @Operation(description = "Creates a new offender-match entity associated with a case and a defendant ID")
    @PostMapping(value = "/case/{caseId}/defendant/{defendantId}/grouped-offender-matches", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Deprecated(forRemoval = true)
    public @ResponseBody
    Mono<ResponseEntity<Object>> createGroupedOffenderMatchesByDefendant(@PathVariable String caseId,
                                                                         @PathVariable String defendantId,
                                                                         @Valid @RequestBody GroupedOffenderMatchesRequest request) {
        return offenderMatchService.createOrUpdateGroupedMatchesByDefendant(caseId, defendantId, request)
                .map(match -> ResponseEntity.created(URI.create(String.format("/case/%s/defendant/%s/grouped-offender-matches/%s", caseId, defendantId, match.getId())))
                        .build());
    }

    @Operation(description = "Gets an existing offender-match entity associated with a case")
    @GetMapping(value = "/case/{caseId}/defendant/{defendantId}/grouped-offender-matches/{groupId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Deprecated(forRemoval = true)
    public @ResponseBody
    Mono<GroupedOffenderMatchesEntity> getOffenderMatchesByCaseId(@PathVariable String caseId,
                                                                  @PathVariable String defendantId,
                                                                  @PathVariable Long groupId) {
        return offenderMatchService.getGroupedMatchesByCaseId(caseId, defendantId, groupId);
    }

    @Operation(description = "Returns all possible matches found for a given case and defendant ID")
    @GetMapping(value = "/case/{caseId}/defendant/{defendantId}/matchesDetail", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    OffenderMatchDetailResponse getOffenderMatchesDetailByCaseAndDefendantId(@PathVariable String caseId,
                                                                             @PathVariable String defendantId) {
        return offenderMatchService.getOffenderMatchDetailsByCaseIdAndDefendantId(caseId, defendantId);
    }

    @Operation(description = "Gets an existing grouped-offender-match entity associated with a defendant and a group")
    @GetMapping(value = "/defendant/{defendantId}/grouped-offender-matches/{groupId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Mono<GroupedOffenderMatchesEntity> getGroupedOffenderMatchesByDefendantIdAndGroupId(@PathVariable String defendantId,
                                                                                        @PathVariable Long groupId) {
        return offenderMatchService.getGroupedOffenderMatchesByDefendantIdAndGroupId(defendantId, groupId);
    }

    @Operation(description = "Creates a new grouped-offender-match entity associated with a defendant")
    @PostMapping(value = "/defendant/{defendantId}/grouped-offender-matches", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    Mono<ResponseEntity<Object>> createGroupedOffenderMatchesByDefendant(@PathVariable String defendantId,
                                                                         @Valid @RequestBody GroupedOffenderMatchesRequest request) {
        return offenderMatchService.createOrUpdateGroupedMatchesByDefendant(defendantId, request)
                .map(match -> ResponseEntity.created(URI.create(String.format("/defendant/%s/grouped-offender-matches/%s", defendantId, match.getId())))
                        .build());
    }
}
