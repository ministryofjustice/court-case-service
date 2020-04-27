package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@ApiModel("Assessment")
@Getter
@Builder
public class Assessment {
    private String type;
    private LocalDateTime completed;
}
