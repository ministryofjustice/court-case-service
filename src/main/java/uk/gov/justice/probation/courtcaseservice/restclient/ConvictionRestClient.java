package uk.gov.justice.probation.courtcaseservice.restclient;

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
import uk.gov.justice.probation.courtcaseservice.controller.model.CurrentOrderHeaderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.AttendanceMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiAttendances;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCurrentOrderHeaderDetailResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;

import java.util.List;

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

    @Value("${community-api.current-order-header-url-template}")
    private String currentOrderHeaderTemplate;

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
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleConvictionError(crn, convictionId, clientResponse))
            .bodyToMono(CommunityApiAttendances.class)
            .doOnError(e -> log.error(String.format(ERROR_MSG_FORMAT, "sentence attendance", crn, convictionId), e))
            .map(attendances -> attendanceMapper.attendancesFrom(attendances, crn, convictionId));
    }


    public Mono<Conviction> getConviction(final String crn, final Long convictionId) {
        final String path = String.format(convictionUrlTemplate, crn, convictionId);
        return clientHelper.get(path)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleConvictionError(crn, convictionId, clientResponse))
            .bodyToMono(CommunityApiConvictionResponse.class)
            .doOnError(e -> log.error(String.format(ERROR_MSG_FORMAT, "conviction", crn, convictionId), e))
            .map(convictionResponse -> offenderMapper.convictionFrom(convictionResponse));
    }


    public Mono<CurrentOrderHeaderResponse> getCurrentOrderHeaderDetail(final String crn, final Long convictionId, final Long sentenceId) {
        final String path = String.format(currentOrderHeaderTemplate, crn, convictionId, sentenceId);
        return clientHelper.get(path)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleCurrentOrderHeaderError(crn, convictionId, sentenceId, clientResponse))
                .bodyToMono(CommunityApiCurrentOrderHeaderDetailResponse.class)
                .doOnError(e -> log.error(String.format(ERROR_MSG_FORMAT, "sentence current order header detail ", crn, convictionId), e))
                .map(currentOrderHeaderDetailResponse -> offenderMapper.buildCurrentOrderHeaderDetail(currentOrderHeaderDetailResponse));
    }
}
