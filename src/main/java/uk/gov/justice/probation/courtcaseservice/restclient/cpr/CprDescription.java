package uk.gov.justice.probation.courtcaseservice.restclient.cpr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CprDescription {

    private String code;
    private String description;
}