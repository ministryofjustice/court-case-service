package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCourtReport;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiReportManager;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiStaff;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CourtReportMapperTest {

    private static final String PSR_CODE = "CJF";
    private final KeyValue psrType = KeyValue.builder()
            .description("Pre-Sentence Report - Fast")
            .code(PSR_CODE)
            .build();

    @Test
    void whenMapCourtReport_thenReturn() {

        var requestedDate = LocalDate.of(2020, Month.MAY, 1);
        var receivedDate = LocalDate.of(2020, Month.MAY, 3);
        var requiredDate = LocalDate.of(2020, Month.MAY, 4);
        var completedDate = LocalDate.of(2020, Month.MAY, 2);
        var inactiveReportManager = CommunityApiReportManager.builder().active(false).build();
        var activeReportManager = CommunityApiReportManager.builder().active(true)
                            .staff(CommunityApiStaff.builder().forenames("Charles").surname("Dickens").build())
                            .build();

        var communityApiCourtReport = CommunityApiCourtReport.builder()
            .courtReportId(1L)
            .receivedByCourtDate(LocalDateTime.of(receivedDate, LocalTime.of(9,15)))
            .requestedDate(LocalDateTime.of(requestedDate, LocalTime.of(9,15)))
            .requiredDate(LocalDateTime.of(requiredDate, LocalTime.of(9,15)))
            .completedDate(LocalDateTime.of(completedDate, LocalTime.of(9,15)))
            .courtReportType(psrType)
            .deliveredCourtReportType(KeyValue.builder().code("PSR").description("Pre-sentence report").build())
            .reportManagers(List.of(inactiveReportManager, activeReportManager))
            .build();

        var courtReport = CourtReportMapper.buildCourtReport(communityApiCourtReport);

        assertThat(courtReport.getRequestedDate()).isEqualTo(requestedDate);
        assertThat(courtReport.getRequiredDate()).isEqualTo(requiredDate);
        assertThat(courtReport.getCompletedDate()).isEqualTo(completedDate);
        assertThat(courtReport.getCourtReportId()).isEqualTo(1);
        assertThat(courtReport.getCourtReportType().getCode()).isEqualTo(PSR_CODE);
        assertThat(courtReport.getCourtReportType().getDescription()).isEqualTo("Pre-Sentence Report - Fast");
        assertThat(courtReport.getDeliveredCourtReportType().getCode()).isEqualTo("PSR");
        assertThat(courtReport.getDeliveredCourtReportType().getDescription()).isEqualTo("Pre-sentence report");
        assertThat(courtReport.getAuthor().getSurname()).isEqualTo("Dickens");
        assertThat(courtReport.getAuthor().getForenames()).isEqualTo("Charles");
        assertThat(courtReport.getAuthor().isUnallocated()).isEqualTo(false);

    }

    @Test
    void givenMultipleReportManagers_whenMap_chooseFirstActive() {
        var staff1 = CommunityApiStaff.builder().forenames("Unallocated").surname("Staff (N02)").unallocated(true).build();
        var staff2 = CommunityApiStaff.builder().forenames("George Alan").surname("O'Dowd").unallocated(false).build();
        var staff3 = CommunityApiStaff.builder().forenames("Simon").surname("Le Bon").unallocated(false).build();
        var courtReportManager1 = CommunityApiReportManager.builder().active(false)
            .staff(staff1).build();
        var courtReportManager2 = CommunityApiReportManager.builder().active(true)
            .staff(staff2).build();
        var courtReportManager3 = CommunityApiReportManager.builder().active(true)
            .staff(staff3).build();

        var reportAuthor = CourtReportMapper.staffAuthorNameOf(List.of(courtReportManager1, courtReportManager2, courtReportManager3));

        assertThat(reportAuthor.getForenames()).isEqualTo("George Alan");
        assertThat(reportAuthor.getSurname()).isEqualTo("O'Dowd");
        assertThat(reportAuthor.isUnallocated()).isFalse();
    }

}
