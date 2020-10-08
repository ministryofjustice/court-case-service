package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddressRequest {
    private final String line1;
    private final String line2;
    private final String line3;
    private final String line4;
    private final String line5;
    private final String postcode;
}
