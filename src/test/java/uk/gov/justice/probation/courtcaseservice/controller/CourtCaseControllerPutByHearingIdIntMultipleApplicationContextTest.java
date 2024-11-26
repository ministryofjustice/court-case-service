package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.web.reactive.server.WebTestClient;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade;
import uk.gov.justice.probation.courtcaseservice.testUtil.ConcurrentRequestUtil;
import uk.gov.justice.probation.courtcaseservice.testUtil.MultiApplicationContextExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MultiApplicationContextExtension.class)
class CourtCaseControllerPutByHearingIdIntMultipleApplicationContextTest extends BaseIntTest {
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    HearingRepositoryFacade courtCaseRepository;

    private static final String PUT_BY_HEARING_ID_ENDPOINT = "/hearing/{hearingId}";

    private static final String JSON_HEARING_ID = "75e63d6c-5487-4244-a5bc-7cf8a38992db";

    @Value("classpath:integration/request/PUT_courtCaseExtended_success.json")
    private Resource caseDetailsExtendedResource;

    @BeforeEach
    void beforeEach() {
        getEmittedEventsQueueSqsClient().purgeQueue(
            PurgeQueueRequest.builder()
                .queueUrl(getEmittedEventsQueueUrl())
                .build());
    }

    @Test
    void duplicate_hearings() throws ExecutionException, InterruptedException {
        Optional<HearingEntity> hearingDoesNotExist = courtCaseRepository.findFirstByHearingId("75e63d6c-5487-4244-a5bc-7cf8a38992db");
        assertThat(hearingDoesNotExist.isEmpty()).isTrue();
        List<String> ports = List.of("8080" ,"8084");

        ConcurrentRequestUtil.runConcurrentRequests(webTestClient, 20,  ports, "http://localhost:{port}" + PUT_BY_HEARING_ID_ENDPOINT.replace("{hearingId}", JSON_HEARING_ID), caseDetailsExtendedResource);

        HearingEntity hearing = courtCaseRepository.findByHearingIdAndDefendantId("75e63d6c-5487-4244-a5bc-7cf8a38992db","d1eefed2-04df-11ec-b2d8-0242ac130002").get();
        assertThat(hearing.getSourceType()).isEqualTo(SourceType.COMMON_PLATFORM);

    }

}
