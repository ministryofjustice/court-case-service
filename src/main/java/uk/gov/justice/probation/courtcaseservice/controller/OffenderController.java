package uk.gov.justice.probation.courtcaseservice.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class OffenderController {

    @Autowired
    private OffenderService offenderService;

    @GetMapping(path="offender/{crn}/probation-record", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody Offender getOffender(@PathVariable String crn) {
        return offenderService.getOffender(crn);
    }

    @GetMapping(path="offender/{crn}/convictions/{convictionId}/requirements", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    Mono<List<Requirement>> getRequirements(@PathVariable String crn, @PathVariable String convictionId) {
        return offenderService.getConvictionRequirements(crn, convictionId);
    }
}
