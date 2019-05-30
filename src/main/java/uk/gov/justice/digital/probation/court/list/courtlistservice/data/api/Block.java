package uk.gov.justice.digital.probation.court.list.courtlistservice.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Block {
    private String id;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<Case> cases;
}
