package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCourtReport;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCourtReportsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiReportManager;
import uk.gov.justice.probation.courtcaseservice.service.model.CourtReport;
import uk.gov.justice.probation.courtcaseservice.service.model.CourtReport.ReportAuthor;


public class CourtReportMapper {

    public static List<CourtReport> courtReportsFrom(final CommunityApiCourtReportsResponse response) {

        return Optional.ofNullable(response.getCourtReports()).orElse(Collections.emptyList())
                            .stream()
                            .map(CourtReportMapper::buildCourtReport)
                            .collect(Collectors.toList());
    }

    static CourtReport buildCourtReport(final CommunityApiCourtReport courtReport) {

        return CourtReport.builder()
            .requestedDate(courtReport.getRequestedDate().toLocalDate())
            .requiredDate(courtReport.getRequiredDate().toLocalDate())
            .completedDate(Optional.ofNullable(courtReport.getCompletedDate())
                                    .map(LocalDateTime::toLocalDate)
                                    .orElse(null))
            .courtReportType(courtReport.getCourtReportType())
            .courtReportId(courtReport.getCourtReportId())
            .author(staffAuthorNameOf(courtReport.getReportManagers()))
            .build();
    }

    static ReportAuthor staffAuthorNameOf(final List<CommunityApiReportManager> reportManagers) {
        return Optional.ofNullable(reportManagers).orElse(Collections.emptyList())
            .stream()
            .filter(CommunityApiReportManager::isActive)
            .findFirst()
            .map(CommunityApiReportManager::getStaff)
            .map(communityApiStaff -> ReportAuthor.builder().forenames(communityApiStaff.getForenames())
                                        .surname(communityApiStaff.getSurname())
                                        .unallocated(communityApiStaff.isUnallocated())
                                        .build())
            .orElse(ReportAuthor.builder().unallocated(true).build());
    }

}
