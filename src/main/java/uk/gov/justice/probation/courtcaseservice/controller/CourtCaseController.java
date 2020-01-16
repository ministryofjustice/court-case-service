package uk.gov.justice.probation.courtcaseservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseListResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
public class CourtCaseController {

    private final CourtCaseService courtCaseService;

    public CourtCaseController(CourtCaseService courtCaseService) {
        this.courtCaseService = courtCaseService;
    }

    @GetMapping(value = "/court/{courtCode}/case/{caseNo}", produces = "application/json")
    public @ResponseBody
    CourtCaseEntity courtCase(@PathVariable String courtCode, @PathVariable String caseNo) {
        return courtCaseService.getCaseByCaseNumber(courtCode, caseNo);
    }

    @PutMapping("/case/{id}")
    public @ResponseBody
    CourtCaseEntity updateCase(@PathVariable(value = "id") Long caseId, @Valid @RequestBody CourtCaseEntity courtCaseDetails) {
        return courtCaseService.createOrUpdateCase(caseId, courtCaseDetails);
    }

    @GetMapping(value = "/court/{courtCode}/cases", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    CaseListResponse getCaseList(@PathVariable String courtCode,
                                 @RequestParam("date")
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {
        List<CourtCaseEntity> courtCases = courtCaseService.filterCasesByCourtAndDate(courtCode, date);
        return new CaseListResponse(courtCases);
    }
}
