package uk.gov.justice.digital.probation.court.list.courtlistservice.data.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
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
    private List<Offence> offences;
}
