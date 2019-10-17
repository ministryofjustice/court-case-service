package uk.gov.justice.probation.courtlistservice.prototype.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.probation.courtlistservice.prototype.data.api.CourtList;
import uk.gov.justice.probation.courtlistservice.prototype.service.CourtListService;

import java.time.LocalDate;

@RestController
@Slf4j
public class CourtListController {
    private final CourtListService courtListService;

    public CourtListController(CourtListService courtListService) {
        this.courtListService = courtListService;
    }

    @RequestMapping(value="/court/{court}/list")
    public CourtList courtList(
            @PathVariable String court,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("date") LocalDate date) {

        log.info("Court list requested for court {} for date {}", court, date);
        return courtListService.courtList(court, date);
    }

    @RequestMapping(value="/court/list")
    public CourtList courtList() {

        log.info("Court list requested for all court lists");
        return courtListService.allCourtLists();
    }
}
