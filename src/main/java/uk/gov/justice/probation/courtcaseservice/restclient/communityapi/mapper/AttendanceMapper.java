package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse.ContactTypeDetail;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiAttendance;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiAttendance.CommunityApiContactTypeDetail;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiAttendances;

public class AttendanceMapper {

    public static List<AttendanceResponse> attendancesFrom(final CommunityApiAttendances communityApiAttendances) {

        final List<CommunityApiAttendance> sourceAttendances = Optional.ofNullable(communityApiAttendances.getAttendances())
            .orElse(Collections.emptyList());

        return sourceAttendances.stream()
                            .map(AttendanceMapper::buildAttendance)
                            .collect(Collectors.toList());
    }

    static AttendanceResponse buildAttendance(final CommunityApiAttendance attendance) {

        final Optional<CommunityApiContactTypeDetail> typeDetail = Optional.ofNullable(attendance.getContactType());

        return AttendanceResponse.builder()
            .attendanceDate(attendance.getAttendanceDate())
            .attended(attendance.isAttended())
            .complied(attendance.isComplied())
            .contactId(attendance.getContactId())
            .outcome(attendance.getOutcome())
            .contactType(ContactTypeDetail.builder()
                            .code(typeDetail.map(CommunityApiContactTypeDetail::getCode).orElse(null))
                            .description(typeDetail.map(CommunityApiContactTypeDetail::getDescription).orElse(null))
                            .build())
            .build();

    }

}
