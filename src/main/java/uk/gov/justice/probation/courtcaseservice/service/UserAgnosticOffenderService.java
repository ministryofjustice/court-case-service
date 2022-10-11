package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class UserAgnosticOffenderService {

    private final OffenderRestClient userAgnosticOffenderRestClient;

    private final OffenderRepository offenderRepository;

    private final TelemetryService telemetryService;

    @Autowired
    public UserAgnosticOffenderService(final OffenderRestClientFactory offenderRestClientFactory,
                                       final OffenderRepository offenderRepository, TelemetryService telemetryService) {
        this.userAgnosticOffenderRestClient = offenderRestClientFactory.buildUserAgnosticOffenderRestClient();
        this.offenderRepository = offenderRepository;
        this.telemetryService = telemetryService;
    }


    public Mono<ProbationStatusDetail> getProbationStatusWithoutRestrictions(String crn) {
        return userAgnosticOffenderRestClient.getProbationStatusByCrn(crn);
    }

    public Optional<OffenderEntity> updateOffenderProbationStatus(String crn) {
        return offenderRepository.findByCrn(crn)
                .map(offenderEntity -> {
                    ProbationStatusDetail probationStatusDetail = getProbationStatusWithoutRestrictions(crn).block();
                    if (probationStatusDetail != null && !Objects.equals(probationStatusDetail, offenderEntity.getProbationStatusDetail())) {
                        updateProbationStatusDetails(probationStatusDetail, offenderEntity);
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

    private OffenderEntity updateProbationStatusDetails(ProbationStatusDetail probationStatusDetail, OffenderEntity offender) {
        offender.setProbationStatus(OffenderProbationStatus.of(probationStatusDetail.getStatus()));
        offender.setPreviouslyKnownTerminationDate(probationStatusDetail.getPreviouslyKnownTerminationDate());
        Optional.ofNullable(probationStatusDetail.getInBreach()).ifPresent(offender::setBreach);
        Optional.ofNullable(probationStatusDetail.getAwaitingPsr()).ifPresent(offender::setAwaitingPsr);
        offender.setPreSentenceActivity(probationStatusDetail.isPreSentenceActivity());
        return offender;
    }

}
