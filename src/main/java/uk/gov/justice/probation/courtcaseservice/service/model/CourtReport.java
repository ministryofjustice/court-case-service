package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Schema(description = "Court Report")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CourtReport {

    private final Long courtReportId;
    private final LocalDate requestedDate;
    private final LocalDate requiredDate;
    private final LocalDate completedDate;
    private final KeyValue courtReportType;
    private final KeyValue deliveredCourtReportType;
    private final ReportAuthor author;

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
    public static class ReportAuthor {
        private final boolean unallocated;
        private final String forenames;
        private final String surname;
    }

    public boolean isTypeOneOf(List<String> reportTypeCodes) {
        return Optional.ofNullable(reportTypeCodes)
            .map(codes -> codes.contains(courtReportType.getCode()))
            .orElse(false);
    }
}
