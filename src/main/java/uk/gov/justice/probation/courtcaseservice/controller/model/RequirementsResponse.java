package uk.gov.justice.probation.courtcaseservice.controller.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

@ApiModel("Lists of Requirements")
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequirementsResponse {
    private final List<Requirement> requirements;
}
