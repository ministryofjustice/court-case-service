package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class HearingCourtCaseId implements Serializable {
    private String hearingId;
    private String caseId;
}
