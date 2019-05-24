package uk.gov.justice.digital.probation.court.list.courtlistservice.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class Session {
    private String id;
    private LocalDate dateOfHearing;
    private String localJusticeArea;
    private String courtName;
    private String courtRoom;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<Block> blocks;
}
