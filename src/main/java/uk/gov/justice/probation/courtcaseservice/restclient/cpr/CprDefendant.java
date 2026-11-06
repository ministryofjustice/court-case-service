package uk.gov.justice.probation.courtcaseservice.restclient.cpr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CprDefendant {

    private String firstName;
    private String middleNames;
    private String lastName;
    private String dateOfBirth;
    private CprDescription title;
    private CprDescription sex;
    private List<CprAddress> addresses;
    private List<CprAlias> aliases;
    private CprIdentifiers identifiers;

    public List<CprAddress> getAddresses() {
        return addresses == null
                ? Collections.emptyList()
                : addresses;
    }

    public List<CprAlias> getAliases() {
        return aliases == null
                ? Collections.emptyList()
                : aliases;
    }

    public List<String> getCrns() {
        return identifiers == null
                ? Collections.emptyList()
                : identifiers.getCrns();
    }
}