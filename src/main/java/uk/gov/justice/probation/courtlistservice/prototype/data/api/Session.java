package uk.gov.justice.probation.courtlistservice.prototype.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    private String id;
    private LocalDate dateOfHearing;
    private String localJusticeArea;
    private String crimeManagementUnit;
    private String courtName;
    private String courtRoom;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<Block> blocks;
}
