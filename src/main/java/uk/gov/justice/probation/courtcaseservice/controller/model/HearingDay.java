package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
public class HearingDay {
    @NotBlank
    private final String courtCode;
    @NotBlank
    private final String courtRoom;
    @NotNull
    private final LocalDateTime sessionStartTime;
    @Deprecated(forRemoval = true)
    private final String listNo;
}
