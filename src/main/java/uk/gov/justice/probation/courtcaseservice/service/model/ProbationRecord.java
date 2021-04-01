package uk.gov.justice.probation.courtcaseservice.service.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("Probation Record")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProbationRecord {
    private final String crn;
    private final List<OffenderManager> offenderManagers;
    @Setter
    private List<Conviction> convictions;
    @Setter
    private Assessment assessment;
}
