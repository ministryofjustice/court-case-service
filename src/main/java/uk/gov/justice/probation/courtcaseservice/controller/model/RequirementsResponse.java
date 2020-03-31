package uk.gov.justice.probation.courtcaseservice.controller.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import java.util.List;

@AllArgsConstructor
@Getter
public class RequirementsResponse {
    List<Requirement> requirements;
}
