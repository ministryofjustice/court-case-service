package uk.gov.justice.probation.courtcaseservice.controller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;


@RestController
@Slf4j
public class CourtCaseController {

    private final CourtCaseService courtCaseService;

    public CourtCaseController(CourtCaseService courtCaseService) {
        this.courtCaseService = courtCaseService;
    }

    @GetMapping(value="/court/{courtCode}/case/{caseNo}", produces = "application/json")
    public @ResponseBody CourtCaseEntity courtCase(
            @PathVariable String courtCode, @PathVariable String caseNo)
                 {

        log.info("Court case requested for court {} for case number {}", courtCode, caseNo);
        return courtCaseService.getCaseByCaseNumber(courtCode, caseNo);
    }

}
