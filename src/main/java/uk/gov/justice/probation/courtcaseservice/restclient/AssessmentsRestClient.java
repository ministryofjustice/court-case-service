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
import uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.mapper.AssessmentMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.model.AssessmentsApiAssessmentResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;


@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class AssessmentsRestClient {
    @Value("${offender-assessments-api.latest-assessment-crn-url-template}")
    private String assessmentsUrlTemplate;

    @Autowired
    private AssessmentMapper mapper;

    @Autowired
    @Qualifier("assessmentsApiClient")
    private RestClientHelper clientHelper;

    public Mono<Assessment> getAssessmentByCrn(String crn) {
        return clientHelper.get(String.format(assessmentsUrlTemplate, crn))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleError(crn, clientResponse))
            .bodyToMono(AssessmentsApiAssessmentResponse.class)
            .doOnError(e -> log.error(String.format("Unexpected exception when retrieving offender assessment data for CRN '%s'", crn), e))
            .map(assessmentResponse -> mapper.assessmentFrom(assessmentResponse));
    }
}
