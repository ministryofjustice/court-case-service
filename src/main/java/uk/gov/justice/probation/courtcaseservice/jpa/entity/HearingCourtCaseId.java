package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class HearingCourtCaseId implements Serializable {
    private String hearingId;
    private String caseId;

    public HearingCourtCaseId() {}

    public HearingCourtCaseId(String hearingId, String caseId) {
        this.hearingId = hearingId;
        this.caseId = caseId;
    }
}
