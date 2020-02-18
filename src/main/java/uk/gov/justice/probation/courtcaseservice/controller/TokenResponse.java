package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
}
