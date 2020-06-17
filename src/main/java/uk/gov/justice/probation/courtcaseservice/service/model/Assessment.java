package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
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
}
