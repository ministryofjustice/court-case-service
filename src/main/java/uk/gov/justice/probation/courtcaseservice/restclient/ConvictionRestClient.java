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
import uk.gov.justice.probation.courtcaseservice.controller.model.CurrentOrderHeaderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.AttendanceMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiAttendances;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCustodialStatusResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.CustodialStatusNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

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
    private String custodialStatusUrlTemplate;

    @Autowired
    @Qualifier("communityApiClient")
    private RestClientHelper clientHelper;

    public Mono<List<AttendanceResponse>> getAttendances(final String crn, final Long convictionId) {
        final String path = String.format(convictionAttendanceUrlTemplate, crn, convictionId);
        return clientHelper.get(path)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleConvictionError(crn, convictionId, clientResponse))
            .bodyToMono(CommunityApiAttendances.class)
            // TODO: doOnError will swallow the exception and fail later - use onErrorMap
            .doOnError(e -> log.error(String.format(ERROR_MSG_FORMAT, "sentence attendance", crn, convictionId), e))
            .map(attendances -> AttendanceMapper.attendancesFrom(attendances));
    }


    public Mono<Conviction> getConviction(final String crn, final Long convictionId) {
        final String path = String.format(convictionUrlTemplate, crn, convictionId);
        return clientHelper.get(path)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleConvictionError(crn, convictionId, clientResponse))
            .bodyToMono(CommunityApiConvictionResponse.class)
            // TODO: doOnError will swallow the exception and fail later - use onErrorMap
            .doOnError(e -> log.error(String.format(ERROR_MSG_FORMAT, "conviction", crn, convictionId), e))
            .map(OffenderMapper::convictionFrom);
    }


    public Mono<CurrentOrderHeaderResponse> getCurrentOrderHeader(final String crn, final Long convictionId, final Long sentenceId) {
        final String path = String.format(custodialStatusUrlTemplate, crn, convictionId, sentenceId);
        return clientHelper.get(path)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleCustodialStatusError(crn, convictionId, sentenceId, clientResponse))
                .bodyToMono(CommunityApiCustodialStatusResponse.class)
                // TODO: Remove onErrorResume step - This fallback is a temporary measure pending fix of PIC-674.
                // The community api will return a 404 if the offender has no custody associated with their sentence but it
                // should return a status of 'Not in custody'. This error handling is a temporary measure to allow graceful
                // degradation when this happens
                .onErrorResume(CustodialStatusNotFoundException.class, e -> Mono.just(buildNotInCustodyResponse(sentenceId)))
                .onErrorMap(e1 -> {
                    log.error(String.format(ERROR_MSG_FORMAT, "sentence current order header detail ", crn, convictionId), e1);
                    return e1;
                })
                .map(OffenderMapper::buildCurrentOrderHeaderDetail);
    }

    public CommunityApiCustodialStatusResponse buildNotInCustodyResponse(Long sentenceId) {

        return CommunityApiCustodialStatusResponse.builder()
                .sentenceId(sentenceId)
                .custodialType(KeyValue.builder()
                        .code("NOT_IN_CUSTODY")
                        .description("Not in custody")
                        .build())
                .build();
    }
}
