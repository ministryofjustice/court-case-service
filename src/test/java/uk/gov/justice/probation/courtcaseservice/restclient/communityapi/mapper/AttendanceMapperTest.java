package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse.ContactTypeDetail;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiAttendance;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiAttendance.CommunityApiContactTypeDetail;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiAttendances;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceMapperTest {

    private static final Long SOME_CONTACT_ID_1 = 875L;
    private static final Long SOME_CONTACT_ID_2 = 1231232L;


    @DisplayName("Maps Community API with null list to court case service object instance")
    @Test
    void mapNullAttendances() {

        final CommunityApiAttendances apiAttendances = CommunityApiAttendances.builder().build();

        // Act
        final List<AttendanceResponse> response = AttendanceMapper.attendancesFrom(apiAttendances);

        // Assert
        assertThat(response).isEmpty();
    }

    @DisplayName("Maps Community API with multiple attendances in list to court case service object instance")
    @Test
    void mapMultipleAttendances() {

        // Arrange
        final LocalDate now = LocalDate.now();
        final CommunityApiAttendance apiAttendance1 = CommunityApiAttendance.builder()
            .attendanceDate(now).complied(true).attended(true).contactId(SOME_CONTACT_ID_1).outcome("Outcome1").build();
        final CommunityApiAttendance apiAttendance2 = CommunityApiAttendance.builder()
            .attendanceDate(now).contactId(SOME_CONTACT_ID_2).outcome("Outcome2")
            .contactType(CommunityApiContactTypeDetail.builder().code("CODE2").description("DESC2").build()).build();

        final CommunityApiAttendances apiAttendances = CommunityApiAttendances.builder()
            .attendances(Arrays.asList(apiAttendance1, apiAttendance2))
            .build();

        // Act
        final List<AttendanceResponse> response = AttendanceMapper.attendancesFrom(apiAttendances);

        // Assert
        assertThat(response).doesNotHaveDuplicates();

        final AttendanceResponse expectedResponse1 = AttendanceResponse.builder().attendanceDate(now).attended(true).complied(true)
            .contactId(SOME_CONTACT_ID_1).outcome("Outcome1").contactType(new ContactTypeDetail(null, null)).build();
        assertThat(response).filteredOn("contactId", SOME_CONTACT_ID_1).first().isEqualTo(expectedResponse1);

        final AttendanceResponse expectedResponse2 = AttendanceResponse.builder().attendanceDate(now).attended(false).complied(false)
            .contactId(SOME_CONTACT_ID_2).outcome("Outcome2").contactType(new ContactTypeDetail("DESC2", "CODE2")).build();
        assertThat(response).filteredOn("contactId", SOME_CONTACT_ID_2).first().isEqualTo(expectedResponse2);
    }
}
