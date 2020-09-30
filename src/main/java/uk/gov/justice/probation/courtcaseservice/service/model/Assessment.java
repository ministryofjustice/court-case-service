package uk.gov.justice.probation.courtcaseservice.service.model;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("Assessment")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class Assessment {
    private final String type;
    private final LocalDateTime completed;
    private final String status;
}
