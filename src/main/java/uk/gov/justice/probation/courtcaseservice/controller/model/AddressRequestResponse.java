package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressRequestResponse {
    private final String line1;
    private final String line2;
    private final String line3;
    private final String line4;
    private final String line5;
    private final String postcode;

    public AddressPropertiesEntity asEntity() {
        return AddressPropertiesEntity.builder()
                .line1(line1)
                .line2(line2)
                .line3(line3)
                .line4(line4)
                .line5(line5)
                .postcode(postcode)
                .build();
    }
}
