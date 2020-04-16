package uk.gov.justice.probation.courtcaseservice.restclient;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiGroupedDocumentsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import java.util.List;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class OffenderRestClient {
    @Value("${community-api.offender-by-crn-url-template}")
    private String offenderUrlTemplate;
    @Value("${community-api.convictions-by-crn-url-template}")
    private String convictionsUrlTemplate;
    @Value("${community-api.requirements-by-crn-url-template}")
    private String requirementsUrlTemplate;
    @Value("${community-api.grouped-documents-by-crn-url-template}")
    private String groupedDocumentsUrlTemplate;
    @Autowired
    private OffenderMapper mapper;
    @Autowired
    private RestClientHelper clientHelper;

    public Mono<ProbationRecord> getOffenderByCrn(String crn) {
        return clientHelper.get(String.format(offenderUrlTemplate, crn))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleError(crn, clientResponse))
                .bodyToMono(CommunityApiOffenderResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving offender data for CRN '%s'", crn), e))
                .map(offender -> mapper.offenderFrom(offender));
    }

    public Mono<List<Conviction>> getConvictionsByCrn(String crn) {
        return clientHelper.get(String.format(convictionsUrlTemplate, crn))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleError(crn, clientResponse))
                .bodyToMono(CommunityApiConvictionsResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving convictions data for CRN '%s'", crn), e))
                .map( convictionsResponse -> mapper.convictionsFrom(convictionsResponse));
    }

    public Mono<List<Requirement>> getConvictionRequirements(String crn, String convictionId) {
        return clientHelper.get(String.format(requirementsUrlTemplate, crn, convictionId))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleError(crn, clientResponse))
                .bodyToMono(CommunityApiRequirementsResponse.class)
                .doOnError(e -> log.error(String.format("Unexpected exception when retrieving requirements data for CONVICTIONID '%s'", convictionId), e))
                .map( requirementsResponse -> mapper.requirementsFrom(requirementsResponse));
    }

    public Mono<GroupedDocuments> getDocumentsByCrn(String crn) {
        return clientHelper.get(String.format(groupedDocumentsUrlTemplate, crn))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, (clientResponse) -> clientHelper.handleError(crn, clientResponse))
            .bodyToMono(CommunityApiGroupedDocumentsResponse.class)
            .doOnError(e -> log.error(String.format("Unexpected exception when retrieving grouped document data for CRN '%s'", crn), e))
            .map(documentsResponse -> mapper.documentsFrom(documentsResponse));
    }
}
