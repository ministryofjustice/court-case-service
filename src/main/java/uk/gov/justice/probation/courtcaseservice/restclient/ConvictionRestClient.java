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
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.AttendanceMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.CourtReportMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiAttendances;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCourtReportsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiSentenceStatusResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.CourtReport;
import uk.gov.justice.probation.courtcaseservice.service.model.CustodialStatus;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.SentenceStatus;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class ConvictionRestClient {

    private static final String ERROR_MSG_FORMAT = "Unexpected exception when retrieving %s data for CRN '%s' and conviction id '%s'";

    @Value("${community-api.attendances-by-crn-and-conviction-url-template}")
    private String convictionAttendanceUrlTemplate;

    @Value("${community-api.court-reports-by-crn-and-conviction-url-template}")
    private String courtReportsUrlTemplate;

    @Value("${community-api.conviction-by-crn-url-template}")
    private String convictionUrlTemplate;

    @Value("${community-api.sentence-status-url-template}")
    private String sentenceStatusUrlTemplate;

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
            .map(AttendanceMapper::attendancesFrom);
    }


    public Mono<Conviction> getConviction(final String crn, final Long convictionId) {
        final String path = String.format(convictionUrlTemplate, crn, convictionId);
        return clientHelper.get(path)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleConvictionError(crn, convictionId, clientResponse))
            .bodyToMono(CommunityApiConvictionResponse.class)
            .onErrorMap(e1 -> {
                log.error(String.format(ERROR_MSG_FORMAT, "conviction ", crn, convictionId), e1);
                return e1;
            })
            .map(OffenderMapper::convictionFrom);
    }

    public Mono<SentenceStatus> getSentenceStatus(final String crn, final Long convictionId) {
        final String path = String.format(sentenceStatusUrlTemplate, crn, convictionId);
        return clientHelper.get(path)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleConvictionError(crn, convictionId, clientResponse))
                .bodyToMono(CommunityApiSentenceStatusResponse.class)
                .onErrorMap(e1 -> {
                    log.error(String.format(ERROR_MSG_FORMAT, "sentence current order header detail ", crn, convictionId), e1);
                    return e1;
                })
                .map(OffenderMapper::buildSentenceStatus);
    }

    public Mono<CustodialStatus> getCustodialStatus(final String crn, final Long convictionId) {
        final String path = String.format(sentenceStatusUrlTemplate, crn, convictionId);
        return clientHelper.get(path)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleConvictionError(crn, convictionId, clientResponse))
            .bodyToMono(CommunityApiSentenceStatusResponse.class)
            .onErrorMap(e1 -> {
                log.error(String.format(ERROR_MSG_FORMAT, "sentence status ", crn, convictionId), e1);
                return e1;
            })
            .map(custodialStatusResponse -> {
                var code = Optional.ofNullable(custodialStatusResponse.getCustodialType()).map(KeyValue::getCode).orElse(null);
                return Optional.ofNullable(code)
                    .map(CustodialStatus::fromString)
                    .orElse(CustodialStatus.UNKNOWN);
            })
            ;
    }

    public Mono<List<CourtReport>> getCourtReports(final String crn, final Long convictionId) {
        final String path = String.format(courtReportsUrlTemplate, crn, convictionId);
        return clientHelper.get(path)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleConvictionError(crn, convictionId, clientResponse))
            .bodyToMono(CommunityApiCourtReportsResponse.class)
            .onErrorMap(e1 -> {
                log.error(String.format(ERROR_MSG_FORMAT, "court reports ", crn, convictionId), e1);
                return e1;
            })
            .map(CourtReportMapper::courtReportsFrom)
            ;
    }
}
