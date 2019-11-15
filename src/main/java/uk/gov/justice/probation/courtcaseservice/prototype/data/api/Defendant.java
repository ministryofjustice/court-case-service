package uk.gov.justice.probation.courtcaseservice.prototype.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Defendant {
    private String name;
    private String gender;
    private LocalDate dateOfBirth;
    private Address address;

    public String getAge() {
        return Optional.ofNullable(dateOfBirth).map(dob -> String.valueOf(Period.between(dob, LocalDate.now()).getYears())).orElse(null);
    }
}
