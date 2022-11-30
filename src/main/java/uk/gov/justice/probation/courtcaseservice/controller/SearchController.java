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
import uk.gov.justice.probation.courtcaseservice.service.DefendantSearchService;
import uk.gov.justice.probation.courtcaseservice.service.model.SearchResult;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@OpenAPIDefinition(info =
@Info(
    title = "court-case-service",
    description = "API to search cases by defendant parameters.",
    license = @License(name = "The MIT License (MIT)", url = "https://github.com/ministryofjustice/court-case-service/blob/main/LICENSE")
)
)
@Tag(name = "Court Case Search Resources")
@RestController
public class SearchController {
    private final DefendantSearchService searchService;

    @Autowired
    public SearchController(final DefendantSearchService searchService) {
        this.searchService = searchService;
    }
    @Operation(description = "Searches cases by CRN")
    @GetMapping(value = "/search", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody SearchResult searchByCrn(@RequestParam("crn") String crn) {
        return searchService.searchByCrn(crn);
    }
}
