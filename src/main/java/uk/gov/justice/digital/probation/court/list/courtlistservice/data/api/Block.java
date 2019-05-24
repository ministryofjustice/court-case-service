package uk.gov.justice.digital.probation.court.list.courtlistservice.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class Block {
    private String id;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<Case> cases;
}
