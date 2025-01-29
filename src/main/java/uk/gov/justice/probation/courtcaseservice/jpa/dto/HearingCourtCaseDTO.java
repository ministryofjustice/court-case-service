package uk.gov.justice.probation.courtcaseservice.jpa.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@SuperBuilder
public class HearingCourtCaseDTO {
    private Long id;
    private String hearingId;
    private String caseId;
    private LocalDateTime created;
}
