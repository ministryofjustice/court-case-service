package uk.gov.justice.probation.courtcaseservice.controller.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @NotNull
    private final String listNo;
}
