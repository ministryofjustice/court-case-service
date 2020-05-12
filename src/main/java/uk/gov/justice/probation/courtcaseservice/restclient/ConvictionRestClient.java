package uk.gov.justice.probation.courtcaseservice.restclient;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendanceResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.AttendanceMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiAttendances;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class ConvictionRestClient {

    private static final String ERROR_MSG_FORMAT = "Unexpected exception when retrieving %s data for CRN '%s' and conviction id '%s'";

    @Value("${community-api.attendances-by-crn-and-conviction-url-template}")
    private String convictionAttendanceUrlTemplate;

    @Value("${community-api.conviction-by-crn-url-template}")
    private String convictionUrlTemplate;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private OffenderMapper offenderMapper;

    @Autowired
    @Qualifier("communityApiClient")
    private RestClientHelper clientHelper;

    public Mono<List<AttendanceResponse>> getAttendances(final String crn, final Long convictionId) {
        final String path = String.format(convictionAttendanceUrlTemplate, crn, convictionId);
        return clientHelper.get(path)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleOffenderError(crn, clientResponse))
            .bodyToMono(CommunityApiAttendances.class)
            .doOnError(e -> log.error(String.format(ERROR_MSG_FORMAT, "conviction attendance", crn, convictionId), e))
            .map(attendances -> attendanceMapper.attendancesFrom(attendances, crn, convictionId));
    }


    public Mono<Conviction> getConviction(final String crn, final Long convictionId) {
        final String path = String.format(convictionUrlTemplate, crn, convictionId);
        return clientHelper.get(path)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleOffenderError(crn, clientResponse))
            .bodyToMono(CommunityApiConvictionResponse.class)
            .doOnError(e -> log.error(String.format(ERROR_MSG_FORMAT, "conviction", crn, convictionId), e))
            .map(convictionResponse -> offenderMapper.convictionFrom(convictionResponse));
    }
}
