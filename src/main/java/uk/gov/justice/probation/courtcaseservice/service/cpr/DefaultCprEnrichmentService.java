package uk.gov.justice.probation.courtcaseservice.service.cpr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.cpr.CprDefendant;
import uk.gov.justice.probation.courtcaseservice.restclient.cpr.CprRestClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultCprEnrichmentService implements CprEnrichmentService {

    private final CprRestClient cprRestClient;
    private final DefendantRepository defendantRepository;
    private final CprDefendantMapper cprDefendantMapper;
    private final OffenderSearchEnrichmentService offenderSearchEnrichmentService;

    @Override
    public List<CprEnrichmentOutcome> enrichHearingDefendants(
            HearingEntity hearing
    ) {
        return Optional.ofNullable(hearing.getHearingDefendants())
                .orElse(Collections.emptyList())
                .stream()
                .map(hearingDefendant -> hearingDefendant.getDefendant())
                .filter(defendant -> defendant != null)
                .map(this::enrichIncomingDefendant)
                .toList();
    }

    @Override
    @Transactional
    public CprEnrichmentResult enrich(
            DefendantEntity defendant,
            CprRefreshTarget target
    ) {
        if (defendant == null) {
            return CprEnrichmentResult.failure(
                    target,
                    new IllegalArgumentException("Defendant must not be null")
            );
        }

        if (!target.hasLookupIdentifier()) {
            return CprEnrichmentResult.noLookupIdentifier(target);
        }

        try {
            Optional<CprDefendant> cprRecord =
                    findCprDefendant(target);

            if (cprRecord.isEmpty()) {
                log.info(
                        "No CPR record found for defendant {}",
                        target.defendantId()
                );
                return CprEnrichmentResult.noCprRecord(target);
            }

            enrichDefendant(
                    defendant,
                    cprRecord.get()
            );

            defendantRepository.save(defendant);

            return CprEnrichmentResult.success(target);
        } catch (Exception exception) {
            log.error(
                    "CPR enrichment failed for defendant {}",
                    target.defendantId(),
                    exception
            );

            return CprEnrichmentResult.failure(
                    target,
                    exception
            );
        }
    }

    @Override
    public List<CprEnrichmentResult> enrichAll(
            List<DefendantEntity> defendants,
            CprRefreshSource source
    ) {
        if (defendants == null || defendants.isEmpty()) {
            return Collections.emptyList();
        }

        return defendants.stream()
                .map(defendant -> enrich(
                        defendant,
                        new CprRefreshTarget(
                                defendant.getDefendantId(),
                                defendant.getCId(),
                                source
                        )
                ))
                .toList();
    }

    private CprEnrichmentOutcome enrichIncomingDefendant(
            DefendantEntity defendant
    ) {
        CprRefreshTarget target = new CprRefreshTarget(
                defendant.getDefendantId(),
                defendant.getCId(),
                CprRefreshSource.HEARING_UPSERT
        );

        if (!target.hasLookupIdentifier()) {
            return new CprEnrichmentOutcome(
                    target,
                    CprEnrichmentResult.noLookupIdentifier(target),
                    null
            );
        }

        try {
            Optional<CprDefendant> cprRecord =
                    findCprDefendant(target);

            if (cprRecord.isEmpty()) {
                return new CprEnrichmentOutcome(
                        target,
                        CprEnrichmentResult.noCprRecord(target),
                        null
                );
            }

            CprDefendant cprDefendant = cprRecord.get();

            enrichDefendant(
                    defendant,
                    cprDefendant
            );

            return new CprEnrichmentOutcome(
                    target,
                    CprEnrichmentResult.success(target),
                    cprDefendant
            );
        } catch (Exception exception) {
            log.error(
                    "CPR enrichment failed for incoming defendant {}. " +
                            "The hearing will still be persisted without " +
                            "CPR enrichment",
                    defendant.getDefendantId(),
                    exception
            );

            return new CprEnrichmentOutcome(
                    target,
                    CprEnrichmentResult.failure(
                            target,
                            exception
                    ),
                    null
            );
        }
    }

    private void enrichDefendant(
            DefendantEntity defendant,
            CprDefendant cprDefendant
    ) {
        cprDefendantMapper.map(
                cprDefendant,
                defendant
        );

        List<String> crns = cprDefendant.getCrns();

        /*
         * Preserve the existing matcher behaviour:
         * offender and probation details are applied only when CPR
         * returns exactly one CRN.
         */
        if (crns.size() == 1) {
            enrichExactOffenderMatch(
                    defendant,
                    crns.get(0)
            );
        }
    }

    private void enrichExactOffenderMatch(
            DefendantEntity defendant,
            String crn
    ) {
        offenderSearchEnrichmentService
                .findSingleOffender(crn)
                .ifPresent(offenderDetails -> {
                    OffenderEntity offender =
                            defendant.getOffender();

                    if (offender == null) {
                        offender = OffenderEntity.builder()
                                .crn(crn)
                                .build();
                    }

                    offender.setCrn(crn);
                    offender.setPnc(offenderDetails.pnc());
                    offender.setCro(offenderDetails.cro());
                    offender.setProbationStatus(
                            offenderDetails.probationStatus()
                    );
                    offender.setPreviouslyKnownTerminationDate(
                            offenderDetails
                                    .previouslyKnownTerminationDate()
                    );
                    offender.setBreach(
                            Boolean.TRUE.equals(
                                    offenderDetails.breach()
                            )
                    );
                    offender.setPreSentenceActivity(
                            offenderDetails.preSentenceActivity()
                    );
                    offender.setAwaitingPsr(
                            offenderDetails.awaitingPsr()
                    );

                    defendant.setOffender(offender);
                });
    }

    private Optional<CprDefendant> findCprDefendant(
            CprRefreshTarget target
    ) {
        if (target.cId() != null) {
            return cprRestClient
                    .getByLibraId(target.cId())
                    .blockOptional();
        }

        if (target.defendantId() != null) {
            return cprRestClient
                    .getByCommonPlatformId(target.defendantId())
                    .blockOptional();
        }

        return Optional.empty();
    }
}