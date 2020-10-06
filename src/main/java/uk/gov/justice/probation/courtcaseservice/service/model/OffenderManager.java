package uk.gov.justice.probation.courtcaseservice.service.model;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("Offender Manager")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class OffenderManager {
    private final Staff staff;
    private final LocalDate allocatedDate;
    private final Team team;
    private final String provider;

    @JsonIgnore
    private final boolean active;
    @JsonIgnore
    private final boolean softDeleted;
}
