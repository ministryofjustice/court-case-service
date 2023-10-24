package uk.gov.justice.probation.courtcaseservice.service.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class HearingSearchFilter {
    @NotEmpty
    String courtCode;
    @NotNull
    LocalDate hearingDay;
    String source;
    boolean breach;
}