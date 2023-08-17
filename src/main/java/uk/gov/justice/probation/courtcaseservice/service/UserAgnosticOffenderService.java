package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.client.ProbationStatusDetailRestClient;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class UserAgnosticOffenderService {

    private final ProbationStatusDetailRestClient probationStatusDetailRestClient;

    private final OffenderRepository offenderRepository;

    private final TelemetryService telemetryService;

    @Autowired
    public UserAgnosticOffenderService(final OffenderRestClientFactory offenderRestClientFactory,
                                       final OffenderRepository offenderRepository, TelemetryService telemetryService,
                                       final ProbationStatusDetailRestClient probationStatusDetailRestClient) {
        this.offenderRepository = offenderRepository;
        this.telemetryService = telemetryService;
        this.probationStatusDetailRestClient = probationStatusDetailRestClient;
    }


    public Mono<ProbationStatusDetail> getProbationStatusWithoutRestrictions(String crn) {
        return probationStatusDetailRestClient.getProbationStatusByCrn(crn);
    }

    public Optional<OffenderEntity> updateOffenderProbationStatus(String crn) {
        return offenderRepository.findByCrn(crn)
            .map(offenderEntity -> {
                log.info("Fetching probation status for crn: ", crn);
                ProbationStatusDetail probationStatusDetailFromCommunityApi = getProbationStatusWithoutRestrictions(crn).block();
                if (isUpdateProbationStatus(offenderEntity, probationStatusDetailFromCommunityApi)) {
                    updateProbationStatusDetails(probationStatusDetailFromCommunityApi, offenderEntity);
                    Optional.of(offenderRepository.save(offenderEntity))
                        .map(updatedOffender -> {
                            telemetryService.trackOffenderProbationStatusUpdateEvent(updatedOffender);
                            return updatedOffender;
                        });
                } else {
                    telemetryService.trackOffenderProbationStatusNotUpdateEvent(offenderEntity);
                }
                return offenderEntity;
            });
    }

    private static boolean isUpdateProbationStatus(OffenderEntity offenderEntity, ProbationStatusDetail probationStatusDetailFromCommunityApi) {
        return probationStatusDetailFromCommunityApi != null &&
            !Objects.equals(probationStatusDetailFromCommunityApi, offenderEntity.getProbationStatusDetail()) &&
            !StringUtils.equalsIgnoreCase("NO_RECORD", probationStatusDetailFromCommunityApi.getStatus());
    }

    private OffenderEntity updateProbationStatusDetails(ProbationStatusDetail probationStatusDetail, OffenderEntity offender) {
        offender.setProbationStatus(OffenderProbationStatus.of(probationStatusDetail.getStatus()));
        offender.setPreviouslyKnownTerminationDate(probationStatusDetail.getPreviouslyKnownTerminationDate());
        Optional.ofNullable(probationStatusDetail.getInBreach()).ifPresent(offender::setBreach);
        Optional.ofNullable(probationStatusDetail.getAwaitingPsr()).ifPresent(offender::setAwaitingPsr);
        offender.setPreSentenceActivity(probationStatusDetail.isPreSentenceActivity());
        return offender;
    }

}
