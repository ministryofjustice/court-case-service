package uk.gov.justice.probation.courtcaseservice.controller.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import java.util.List;

@ApiModel("Lists of Requirements")
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequirementsResponse {
    private List<Requirement> requirements;
}
