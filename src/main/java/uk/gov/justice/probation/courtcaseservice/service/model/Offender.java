package uk.gov.justice.probation.courtcaseservice.service.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
public class Offender {
    private String crn;
    private List<OffenderManager> offenderManagers;
    @Setter
    private List<Conviction> convictions;
}
