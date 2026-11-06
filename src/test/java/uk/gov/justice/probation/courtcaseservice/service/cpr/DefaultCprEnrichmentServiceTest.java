package uk.gov.justice.probation.courtcaseservice.service.cpr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.cpr.CprDefendant;
import uk.gov.justice.probation.courtcaseservice.restclient.cpr.CprRestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultCprEnrichmentServiceTest {

    @Mock
    private CprRestClient cprRestClient;
    @Mock
    private DefendantRepository defendantRepository;
    @Mock
    private CprDefendantMapper cprDefendantMapper;
    @Mock
    private OffenderSearchEnrichmentService offenderSearchEnrichmentService;

    private DefaultCprEnrichmentService service;
    private DefendantEntity defendant;
    private CprDefendant cprDefendant;

    @BeforeEach
    void setup() {
        service = new DefaultCprEnrichmentService(
                cprRestClient, defendantRepository, cprDefendantMapper, offenderSearchEnrichmentService);
        defendant = DefendantEntity.builder().defendantId("defendant-id").build();
        cprDefendant = new CprDefendant();
    }

    @Test
    void shouldEnrichHearingDefendantsWithoutSaving() {
        var secondDefendant = DefendantEntity.builder().defendantId("second-defendant-id").build();
        var hearing = HearingEntity.builder()
                .hearingDefendants(List.of(
                        HearingDefendantEntity.builder().defendant(defendant).build(),
                        HearingDefendantEntity.builder().defendant(secondDefendant).build()))
                .build();
        when(cprRestClient.getByCommonPlatformId(defendant.getDefendantId()))
                .thenReturn(Mono.just(cprDefendant));
        when(cprRestClient.getByCommonPlatformId(secondDefendant.getDefendantId()))
                .thenReturn(Mono.just(cprDefendant));

        var outcomes = service.enrichHearingDefendants(hearing);

        assertThat(outcomes).extracting(outcome -> outcome.result().status())
                .containsExactly(CprEnrichmentStatus.SUCCESS, CprEnrichmentStatus.SUCCESS);
        assertThat(outcomes).extracting(outcome -> outcome.target().source())
                .containsOnly(CprRefreshSource.HEARING_UPSERT);
        verify(cprDefendantMapper).map(cprDefendant, defendant);
        verify(cprDefendantMapper).map(cprDefendant, secondDefendant);
        verifyNoInteractions(defendantRepository);
    }

    @Test
    void shouldEnrichWithoutSavingForHearingUpsert() {
        when(cprRestClient.getByCommonPlatformId(defendant.getDefendantId()))
                .thenReturn(Mono.just(cprDefendant));
        var target = new CprRefreshTarget(defendant.getDefendantId(), null, CprRefreshSource.HEARING_UPSERT);

        var result = service.enrich(defendant, target);

        assertThat(result).isEqualTo(CprEnrichmentResult.success(target));
        verify(cprDefendantMapper).map(cprDefendant, defendant);
        verifyNoInteractions(defendantRepository);
    }

    @ParameterizedTest
    @EnumSource(value = CprRefreshSource.class, names = {"NIGHTLY_BATCH", "CPR_EVENT"})
    void shouldSaveForStandaloneRefresh(CprRefreshSource source) {
        when(cprRestClient.getByCommonPlatformId(defendant.getDefendantId()))
                .thenReturn(Mono.just(cprDefendant));
        var target = new CprRefreshTarget(defendant.getDefendantId(), null, source);

        var result = service.enrich(defendant, target);

        assertThat(result).isEqualTo(CprEnrichmentResult.success(target));
        verify(cprDefendantMapper).map(cprDefendant, defendant);
        verify(defendantRepository).save(defendant);
    }

    @ParameterizedTest
    @EnumSource(CprRefreshSource.class)
    void shouldSaveEveryEnrichedDefendantInEnrichAllRegardlessOfSource(CprRefreshSource source) {
        var secondDefendant = DefendantEntity.builder().defendantId("second-defendant-id").build();
        when(cprRestClient.getByCommonPlatformId(defendant.getDefendantId()))
                .thenReturn(Mono.just(cprDefendant));
        when(cprRestClient.getByCommonPlatformId(secondDefendant.getDefendantId()))
                .thenReturn(Mono.just(cprDefendant));

        var results = service.enrichAll(List.of(defendant, secondDefendant), source);

        assertThat(results).containsExactly(
                CprEnrichmentResult.success(new CprRefreshTarget(defendant.getDefendantId(), null, source)),
                CprEnrichmentResult.success(new CprRefreshTarget(secondDefendant.getDefendantId(), null, source)));
        verify(cprDefendantMapper).map(cprDefendant, defendant);
        verify(cprDefendantMapper).map(cprDefendant, secondDefendant);
        verify(defendantRepository).save(defendant);
        verify(defendantRepository).save(secondDefendant);
    }

    @Test
    void shouldNotSaveInEnrichAllWhenNoCprRecordIsFound() {
        when(cprRestClient.getByCommonPlatformId(defendant.getDefendantId())).thenReturn(Mono.empty());

        var results = service.enrichAll(List.of(defendant), CprRefreshSource.NIGHTLY_BATCH);

        assertThat(results).extracting(CprEnrichmentResult::status)
                .containsExactly(CprEnrichmentStatus.NO_CPR_RECORD);
        verifyNoInteractions(cprDefendantMapper, defendantRepository);
    }

    @Test
    void shouldNotSaveInEnrichAllWhenEnrichmentFails() {
        when(cprRestClient.getByCommonPlatformId(defendant.getDefendantId()))
                .thenReturn(Mono.just(cprDefendant));
        doThrow(new IllegalArgumentException("Invalid CPR record"))
                .when(cprDefendantMapper).map(cprDefendant, defendant);

        var results = service.enrichAll(List.of(defendant), CprRefreshSource.NIGHTLY_BATCH);

        assertThat(results).extracting(CprEnrichmentResult::status)
                .containsExactly(CprEnrichmentStatus.FAILED);
        verifyNoInteractions(defendantRepository);
    }

    @Test
    void shouldReportSaveFailureInEnrichAll() {
        when(cprRestClient.getByCommonPlatformId(defendant.getDefendantId()))
                .thenReturn(Mono.just(cprDefendant));
        when(defendantRepository.save(defendant)).thenThrow(new IllegalStateException("Save failed"));

        var results = service.enrichAll(List.of(defendant), CprRefreshSource.HEARING_UPSERT);

        assertThat(results).extracting(CprEnrichmentResult::status)
                .containsExactly(CprEnrichmentStatus.FAILED);
        assertThat(results.getFirst().reason()).isEqualTo("Save failed");
    }
}
