package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@ApiModel("Offender")
@Getter
@Builder
public class Offender {
    private String crn;
    private List<OffenderManager> offenderManagers;
    @Setter
    private List<Conviction> convictions;
}
