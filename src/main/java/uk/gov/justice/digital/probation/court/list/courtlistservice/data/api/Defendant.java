package uk.gov.justice.digital.probation.court.list.courtlistservice.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Defendant {
    private String name;
    private String gender;
    private LocalDate dateOfBirth;
    private Address address;
}
