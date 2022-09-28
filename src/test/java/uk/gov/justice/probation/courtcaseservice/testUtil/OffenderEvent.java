package uk.gov.justice.probation.courtcaseservice.testUtil;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class OffenderEvent {
    private final Long offenderId;
    private String crn;
    private String nomsNumber;
    private Long sourceId;
    private final LocalDateTime eventDateTime ;

}
