package uk.gov.justice.probation.courtlistservice.prototype.data.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Case {
    private String id;
    private String urn;
    private String asn;
    private String caseNumber;
    private String caseSequence;
    private List<String> markers;
    private String listingNumber;
    private String informant;
    private String additionalInformation;
    private String estimatedDuration;
    private Defendant defendant;
    private String bailConditions;
    private String solicitor;
    private List<Offence> offences;
}
