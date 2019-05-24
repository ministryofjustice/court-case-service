package uk.gov.justice.digital.probation.court.list.courtlistservice.data.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Address {
    private String line1;
    private String line2;
    private String line3;
    private String line4;
    private String line5;
    private String postcode;
}
