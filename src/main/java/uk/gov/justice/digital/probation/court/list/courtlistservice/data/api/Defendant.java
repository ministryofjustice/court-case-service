package uk.gov.justice.digital.probation.court.list.courtlistservice.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Defendant {
    private String name;
    private String gender;
    private LocalDate dateOfBirth;
    private Address address;
}
