package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.annotations.ApiModel;
import java.time.LocalDate;
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
    private final String forenames;
    private final String surname;
    private final LocalDate allocatedDate;
}
