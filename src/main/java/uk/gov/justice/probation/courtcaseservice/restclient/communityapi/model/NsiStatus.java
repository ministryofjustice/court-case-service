package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NsiStatus {
    private String code;
    private String description;
}
