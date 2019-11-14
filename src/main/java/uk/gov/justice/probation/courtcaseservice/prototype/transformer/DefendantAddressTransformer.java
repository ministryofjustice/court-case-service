package uk.gov.justice.probation.courtcaseservice.prototype.transformer;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.prototype.data.api.Address;
import uk.gov.justice.probation.courtcaseservice.prototype.data.entity.DefAddrType;

@Component
public class DefendantAddressTransformer {
    public Address toAddress(DefAddrType address) {
        return Address
                .builder()
                .line1(address.getLine1())
                .line2(address.getLine2())
                .line3(address.getLine3())
                .line4(address.getLine4())
                .line5(address.getLine5())
                .postcode(address.getPcode())
                .build();
    }
}
