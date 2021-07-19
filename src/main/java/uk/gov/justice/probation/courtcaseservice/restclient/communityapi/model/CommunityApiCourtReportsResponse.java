package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(converter = CommunityApiCourtReportsConverter.class)
public class CommunityApiCourtReportsResponse {
    private final List<CommunityApiCourtReport> courtReports;
}
