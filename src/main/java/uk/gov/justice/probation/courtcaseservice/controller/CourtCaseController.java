package uk.gov.justice.probation.courtcaseservice.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.probation.courtcaseservice.controller.mapper.CourtCaseResponseMapper;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseListResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
@AllArgsConstructor
public class CourtCaseController {

    private final CourtCaseService courtCaseService;
    private final CourtCaseResponseMapper courtCaseResponseMapper;

    @GetMapping(value = "/court/{courtCode}/case/{caseNo}", produces = "application/json")
    public @ResponseBody
    CourtCaseResponse getCourtCase(@PathVariable String courtCode, @PathVariable String caseNo) {
        return courtCaseResponseMapper.mapFrom(courtCaseService.getCaseByCaseNumber(courtCode, caseNo));
    }

    @PutMapping("/case/{id}")
    public @ResponseBody
    CourtCaseResponse updateCase(@PathVariable(value = "id") String caseId, @Valid @RequestBody CourtCaseEntity courtCaseDetails) {
        return courtCaseResponseMapper.mapFrom(courtCaseService.createOrUpdateCase(caseId, courtCaseDetails));
    }

    @GetMapping(value = "/court/{courtCode}/cases", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    CaseListResponse getCaseList(@PathVariable String courtCode,
                                 @RequestParam("date")
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<CourtCaseEntity> courtCases = courtCaseService.filterCasesByCourtAndDate(courtCode, date);
        List<CourtCaseResponse> courtCaseResponses = courtCases.stream().map(e -> courtCaseResponseMapper.mapFrom(e))
                .collect(Collectors.toList());
        return new CaseListResponse(courtCaseResponses);
    }
}
