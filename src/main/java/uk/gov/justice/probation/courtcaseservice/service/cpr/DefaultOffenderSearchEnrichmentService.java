package uk.gov.justice.probation.courtcaseservice.service.cpr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.client.ProbationStatusDetailRestClient;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;

import java.util.Optional;

@Service
@Slf4j
public class DefaultOffenderSearchEnrichmentService
        implements OffenderSearchEnrichmentService {

    private final OffenderRestClient offenderRestClient;
    private final ProbationStatusDetailRestClient probationStatusDetailRestClient;

    public DefaultOffenderSearchEnrichmentService(
            OffenderRestClientFactory offenderRestClientFactory
    ) {
        this.offenderRestClient =
                offenderRestClientFactory
                        .buildUserAwareOffenderRestClient();

        this.probationStatusDetailRestClient =
                offenderRestClientFactory
                        .buildUserAwareProbationStatusDetailsRestClient();
    }

    @Override
    public Optional<OffenderSearchResult> findSingleOffender(
            String crn
    ) {
        CommunityApiOffenderResponse offender =
                offenderRestClient.getOffender(crn).block();

        if (offender == null) {
            return Optional.empty();
        }

        ProbationStatusDetail probationStatus =
                probationStatusDetailRestClient
                        .getProbationStatusByCrn(crn)
                        .block();

        return Optional.of(
                new OffenderSearchResult(
                        crn,
                        offender.getOtherIds().getPncNumber(),
                        offender.getOtherIds().getCroNumber(),
                        mapStatus(probationStatus),
                        probationStatus == null
                                ? null
                                : probationStatus
                                .getPreviouslyKnownTerminationDate(),
                        probationStatus == null
                                ? null
                                : probationStatus.getInBreach(),
                        probationStatus != null
                                && probationStatus.isPreSentenceActivity(),
                        probationStatus == null
                                ? null
                                : probationStatus.getAwaitingPsr()
                )
        );
    }

    private OffenderProbationStatus mapStatus(
            ProbationStatusDetail probationStatus
    ) {
        return probationStatus == null
                ? null
                : OffenderProbationStatus.of(
                        probationStatus.getStatus()
                );
    }
}