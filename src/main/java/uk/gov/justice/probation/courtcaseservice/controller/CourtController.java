package uk.gov.justice.probation.courtcaseservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@RestController
@Slf4j
public class CourtController {

    private final CourtService courtService;

    @Autowired
    public CourtController(CourtService courtService) {
        this.courtService = courtService;
    }

    @PutMapping(value = "/court/{courtCode}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    CourtEntity updateCourt(@PathVariable(value = "courtCode") @NotEmpty String courtCode, @Valid @RequestBody CourtEntity courtEntity) {
        if (!courtCode.equals(courtEntity.getCourtCode())) {
            throw new ConflictingInputException(String.format("Court code in path '%s' does not match court code in body '%s'", courtCode, courtEntity.getCourtCode()));
        }
        return courtService.updateCourt(courtEntity);
    }

}
