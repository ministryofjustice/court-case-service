package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class AddressPropertiesEntity implements Serializable {
    private String line1;
    private String line2;
    private String postcode;
    private String line3;
    private String line4;
    private String line5;
}
