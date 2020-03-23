package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse.ContactTypeDetail;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendancesResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiAttendance;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiAttendance.CommunityApiContactTypeDetail;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiAttendances;

@Component
public class AttendanceMapper {
    public AttendancesResponse attendancesFrom(final CommunityApiAttendances communityApiAttendances, final String crn, final Long convictionId) {

        final List<CommunityApiAttendance> sourceAttendances = Optional.ofNullable(communityApiAttendances.getAttendances())
            .orElse(Collections.emptyList());

        return AttendancesResponse.builder()
            .crn(crn)
            .convictionId(convictionId)
            .attendances(sourceAttendances
                            .stream()
                            .map(this::buildOffenderManager)
                            .collect(Collectors.toList()))
                .build();
    }

    AttendanceResponse buildOffenderManager(final CommunityApiAttendance attendance) {

        final CommunityApiContactTypeDetail typeDetail = Optional.ofNullable(attendance.getContactType())
            .orElse(CommunityApiContactTypeDetail.builder().build());

        return AttendanceResponse.builder()
            .attendanceDate(attendance.getAttendanceDate())
            .attended(attendance.isAttended())
            .complied(attendance.isComplied())
            .contactId(attendance.getContactId())
            .outcome(attendance.getOutcome())
            .contactType(ContactTypeDetail.builder()
                            .code(typeDetail.getCode())
                            .description(typeDetail.getDescription()).build())
            .build();

    }

}
