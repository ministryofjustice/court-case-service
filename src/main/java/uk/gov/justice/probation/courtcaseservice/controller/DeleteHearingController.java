package uk.gov.justice.probation.courtcaseservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.probation.courtcaseservice.service.DeleteHearingsService;

@RestController
public class DeleteHearingController {
    private final DeleteHearingsService deleteHearingsService;

    @Autowired
    public DeleteHearingController(DeleteHearingsService deleteHearingsService) {
        this.deleteHearingsService = deleteHearingsService;
    }

    @Operation(description = "Sets the deleted value of duplicate hearings")
    @DeleteMapping(value = "/hearing/delete-duplicates")
    @ResponseStatus(HttpStatus.OK)
    public void deleteDuplicateHearings() {
        deleteHearingsService.deleteDuplicateHearings();
    }
}
