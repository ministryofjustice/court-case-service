package uk.gov.justice.probation.courtcaseservice.controller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.probation.courtcaseservice.service.CaseSearchService;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResult;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@OpenAPIDefinition(info =
@Info(
    title = "court-case-service search",
    description = "API to search cases by defendant parameters.",
    license = @License(name = "The MIT License (MIT)", url = "https://github.com/ministryofjustice/court-case-service/blob/main/LICENSE")
)
)
@Tag(name = "Court Case Search Resources")
@RestController
public class CaseSearchController {
    private final CaseSearchService caseSearchService;

    @Autowired
    public CaseSearchController(final CaseSearchService caseSearchService) {
        this.caseSearchService = caseSearchService;
    }

    @Deprecated(forRemoval = true) // should be removed once PaC starts using the fuzzy search endpoint
    @Operation(description = "Search cases by CRN")
    @GetMapping(value = "/search", params = {"crn"}, produces = APPLICATION_JSON_VALUE)
    public @ResponseBody CaseSearchResult searchByCrn(@RequestParam("crn") String crn) {
        return caseSearchService.searchCases(crn);
    }

    @Deprecated(forRemoval = true) // should be removed once PaC starts using the fuzzy search endpoint
    @Operation(description = "Search cases by CRN or name")
    @GetMapping(value = "/search", params = {"term"}, produces = APPLICATION_JSON_VALUE)
    public @ResponseBody CaseSearchResult searchCases(@RequestParam("term") String term) {
        return caseSearchService.searchCases(term);
    }
}
