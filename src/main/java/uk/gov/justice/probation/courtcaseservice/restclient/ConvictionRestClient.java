package uk.gov.justice.probation.courtcaseservice.restclient;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendancesResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.AttendanceMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiAttendances;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class ConvictionRestClient {

    @Value("${community-api.attendances-by-crn-and-conviction-url-template}")
    private String convictionAttendanceUrlTemplate;

    @Autowired
    private AttendanceMapper mapper;

    @Autowired
    private RestClientHelper clientHelper;

    public Mono<AttendancesResponse> getAttendancesByCrnAndConvictionId(final String crn, final Long convictionId) {
        final String path = String.format(convictionAttendanceUrlTemplate, crn, convictionId);
        return clientHelper.get(path)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    if (HttpStatus.NOT_FOUND.equals(clientResponse.statusCode())) {
                        return Mono.error(new OffenderNotFoundException(crn));
                    }
                    else {
                        return Mono.empty();
                    }
                }
            )
            .bodyToMono(CommunityApiAttendances.class)
            .doOnError(e -> log.error(String.format("Unexpected exception when retrieving conviction attendance data for CRN '%s'", crn), e))
            .map(attendances -> mapper.attendancesFrom(attendances, crn, convictionId));
    }

//    public Mono<AttendancesResponse> getAttendancesByCrnAndConvictionId(final String crn, final Long convictionId) {
//        final String path = String.format(convictionAttendanceUrlTemplate, crn, convictionId);
//        return clientHelper.get(path)
//            .exchange()
//            .flatMap(clientResponse -> {
//                    final HttpStatus statusCode = clientResponse.statusCode();
//                    if (HttpStatus.NOT_FOUND.equals(statusCode)) {
//                        return Mono.empty();
//                    statusCode.is5xxServerError()) {
//                        return clientHelper.handleServerError(statusCode, clientResponse.headers().asHttpHeaders(), path);
//                    }
//                    return clientResponse.bodyToMono(CommunityApiAttendances.class)
//                        .doOnError(e -> log.error(String.format("Unexpected exception when retrieving conviction attendance data for CRN '%s'", crn), e))
//                        .map(attendances -> mapper.attendancesFrom(attendances, crn, convictionId));
//                });
//    }

}
