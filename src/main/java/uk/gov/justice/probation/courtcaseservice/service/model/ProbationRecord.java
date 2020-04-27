package uk.gov.justice.probation.courtcaseservice.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@ApiModel("Probation Record")
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProbationRecord {
    private String crn;
    private List<OffenderManager> offenderManagers;
    @Setter
    private List<Conviction> convictions;
}
