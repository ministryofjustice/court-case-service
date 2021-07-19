package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiCourtReport {
    private final Long courtReportId;
    private final Long offenderId;
    private final LocalDateTime requestedDate;
    private final LocalDateTime requiredDate;
    private final LocalDateTime allocationDate;
    private final LocalDateTime completedDate;
    private final LocalDateTime sentToCourtDate;
    private final LocalDateTime receivedByCourtDate;
    private final KeyValue courtReportType;
    private final List<CommunityApiReportManager> reportManagers;
}
