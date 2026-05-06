package uk.gov.justice.probation.courtcaseservice.service;

import kotlin.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceSfoMappingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenceSfoMappingRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SeriousFurtherOffenceFlagResolverTest {

    @Mock
    private OffenceSfoMappingRepository offenceSfoMappingRepository;

    @Mock
    private FeatureFlags featureFlags;

    @InjectMocks
    private SeriousFurtherOffenceFlagResolver seriousFurtherOffenceFlagResolver;

    @BeforeEach
    void enableSeriousFurtherOffence() {
        lenient().when(featureFlags.enableSeriousFurtherOffence()).thenReturn(true);
    }

    @Test
    void buildSeriousFurtherOffenceFlagsMap_queriesRepositoryWithAllOffenceCodesAcrossAllResults() {
        HearingEntity hearing1 = hearingWithOffenceCodes("defendant-id-1", "AB001");
        HearingEntity hearing2 = hearingWithOffenceCodes("defendant-id-2", "CD002");
        CourtCaseEntity case1 = hearing1.getCourtCase();
        CourtCaseEntity case2 = hearing2.getCourtCase();

        given(offenceSfoMappingRepository.findByOffenceCodeIn(anyCollection()))
            .willReturn(List.of(
                OffenceSfoMappingEntity.builder().offenceCode("AB001").seriousFurtherOffenceFlag(true).build(),
                OffenceSfoMappingEntity.builder().offenceCode("CD002").seriousFurtherOffenceFlag(false).build()
            ));

        var result = seriousFurtherOffenceFlagResolver.buildSeriousFurtherOffenceFlagsMap(List.of(
            new Pair<>(case1, hearing1.getHearingDefendants().get(0).getDefendant()),
            new Pair<>(case2, hearing2.getHearingDefendants().get(0).getDefendant())
        ));

        assertThat(result).isEqualTo(Map.of("AB001", true, "CD002", false));
        verify(offenceSfoMappingRepository).findByOffenceCodeIn(Set.of("AB001", "CD002"));
    }

    @Test
    void buildSeriousFurtherOffenceFlagsMap_returnsEmptyMapWhenNoOffenceCodes() {
        HearingEntity hearing = hearingWithNoOffenceCodes("defendant-id-1");

        given(offenceSfoMappingRepository.findByOffenceCodeIn(anyCollection())).willReturn(List.of());

        var result = seriousFurtherOffenceFlagResolver.buildSeriousFurtherOffenceFlagsMap(List.of(
            new Pair<>(hearing.getCourtCase(), hearing.getHearingDefendants().get(0).getDefendant())
        ));

        assertThat(result).isEmpty();
    }

    @Test
    void resolveSeriousFurtherOffenceFlag_returnsTrueWhenAnyOffenceCodeMatchesSfoFlag() {
        HearingEntity hearing = hearingWithOffenceCodes("defendant-id-1", "AB001");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = seriousFurtherOffenceFlagResolver.resolveSeriousFurtherOffenceFlag(hearing.getCourtCase(), defendant, Map.of("AB001", true));

        assertThat(result).isTrue();
    }

    @Test
    void resolveSeriousFurtherOffenceFlag_returnsFalseWhenNoOffenceCodesMatchSfoFlag() {
        HearingEntity hearing = hearingWithOffenceCodes("defendant-id-1", "AB001");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = seriousFurtherOffenceFlagResolver.resolveSeriousFurtherOffenceFlag(hearing.getCourtCase(), defendant, Map.of("AB001", false));

        assertThat(result).isFalse();
    }

    @Test
    void resolveSeriousFurtherOffenceFlag_returnsFalseWhenOffenceCodeNotInMap() {
        HearingEntity hearing = hearingWithOffenceCodes("defendant-id-1", "AB001");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = seriousFurtherOffenceFlagResolver.resolveSeriousFurtherOffenceFlag(hearing.getCourtCase(), defendant, Map.of());

        assertThat(result).isFalse();
    }

    @Test
    void resolveSeriousFurtherOffenceFlag_returnsNullWhenDefendantHasNoOffenceCodes() {
        HearingEntity hearing = hearingWithNoOffenceCodes("defendant-id-1");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = seriousFurtherOffenceFlagResolver.resolveSeriousFurtherOffenceFlag(hearing.getCourtCase(), defendant, Map.of());

        assertThat(result).isNull();
    }

    @Test
    void resolveSeriousFurtherOffenceFlag_returnsTrueWhenAtLeastOneOfMultipleOffenceCodesMatchesSfoFlag() {
        HearingEntity hearing = hearingWithOffenceCodes("defendant-id-1", "AB001", "CD002");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = seriousFurtherOffenceFlagResolver.resolveSeriousFurtherOffenceFlag(hearing.getCourtCase(), defendant, Map.of("AB001", false, "CD002", true));

        assertThat(result).isTrue();
    }

    @Test
    void resolveSeriousFurtherOffenceFlag_returnsFalseWhenFeatureFlagDisabled() {
        given(featureFlags.enableSeriousFurtherOffence()).willReturn(false);
        HearingEntity hearing = hearingWithOffenceCodes("defendant-id-1", "AB001");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = seriousFurtherOffenceFlagResolver.resolveSeriousFurtherOffenceFlag(hearing.getCourtCase(), defendant, Map.of("AB001", true));

        assertThat(result).isFalse();
    }

    private HearingEntity hearingWithNoOffenceCodes(String defendantId) {
        HearingEntity hearing = EntityHelper.aHearingEntityWithCrnAndCaseIdAndHearingId("X123", "case-1", defendantId);
        var offenceWithNoCode = OffenceEntity.builder().title("offence no code").build();
        var updatedDefendant = hearing.getHearingDefendants().get(0).withOffences(List.of(offenceWithNoCode));
        var updatedHearing = hearing.withHearingDefendants(List.of(updatedDefendant));
        CourtCaseEntity courtCase = CourtCaseEntity.builder().hearings(new java.util.ArrayList<>()).build();
        HearingEntity hearingWithCourtCase = updatedHearing.withCourtCase(courtCase);
        courtCase.addHearing(hearingWithCourtCase);
        EntityHelper.refreshMappings(hearingWithCourtCase);
        return hearingWithCourtCase;
    }

    private HearingEntity hearingWithOffenceCodes(String defendantId, String... offenceCodes) {
        HearingEntity hearing = EntityHelper.aHearingEntityWithCrnAndCaseIdAndHearingId("X123", "case-1", defendantId);
        List<OffenceEntity> offences = java.util.Arrays.stream(offenceCodes)
            .map(code -> OffenceEntity.builder().title("offence " + code).offenceCode(code).build())
            .collect(java.util.stream.Collectors.toList());
        var updatedDefendant = hearing.getHearingDefendants().get(0).withOffences(offences);
        var updatedHearing = hearing.withHearingDefendants(List.of(updatedDefendant));
        CourtCaseEntity courtCase = CourtCaseEntity.builder().hearings(new java.util.ArrayList<>()).build();
        var hearingWithCourtCase = updatedHearing.withCourtCase(courtCase);
        courtCase.addHearing(hearingWithCourtCase);
        EntityHelper.refreshMappings(hearingWithCourtCase);
        return hearingWithCourtCase;
    }
}
