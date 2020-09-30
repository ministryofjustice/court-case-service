package uk.gov.justice.probation.courtcaseservice.restclient;

import java.util.List;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.mapper.AssessmentMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.assessmentsapi.model.AssessmentsApiAssessmentsResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Assessment;


@Component
@NoArgsConstructor
@Slf4j
public class AssessmentsRestClient {


    @Value("${offender-assessments-api.assessment-crn-url-template}")
    private String assessmentsUrlTemplate;

    @Autowired
    @Qualifier("assessmentsApiClient")
    private RestClientHelper clientHelper;

    public Mono<List<Assessment>> getAssessmentsByCrn(String crn) {
        return clientHelper.get(String.format(assessmentsUrlTemplate, crn))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleOffenderError(crn, clientResponse))
            .bodyToMono(AssessmentsApiAssessmentsResponse.class)
            .doOnError(e -> log.error(String.format("Unexpected exception when retrieving offender assessment data for CRN '%s'", crn), e))
            .map(AssessmentMapper::assessmentsFrom)
            ;
    }


}
