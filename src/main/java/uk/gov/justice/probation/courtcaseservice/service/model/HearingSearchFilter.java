package uk.gov.justice.probation.courtcaseservice.service.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class HearingSearchFilter {
    @NotEmpty
    String courtCode;
    @NotNull
    LocalDate hearingDay;
    LocalDateTime createdAfter;
    LocalDateTime createdBefore;
    String source;
}