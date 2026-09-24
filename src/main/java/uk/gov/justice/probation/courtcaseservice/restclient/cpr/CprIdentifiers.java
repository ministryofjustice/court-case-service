package uk.gov.justice.probation.courtcaseservice.restclient.cpr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CprIdentifiers {

    private List<String> crns;

    public List<String> getCrns() {
        return crns == null
            ? Collections.emptyList()
            : crns;
    }
}