package uk.gov.justice.probation.courtcaseservice.service;

import kotlin.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceMappaMappingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenceMappaMappingRepository;
import uk.gov.justice.probation.courtcaseservice.service.flags.MultiAgencyPublicProtectionArrangementsFlagResolver;
import uk.gov.justice.probation.courtcaseservice.service.flags.OffenceFlagHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MultiAgencyPublicProtectionArrangementsFlagResolverTest {

    @Mock
    private OffenceMappaMappingRepository offenceMappaMappingRepository;

    @Mock
    private FeatureFlags featureFlags;

    @Spy
    private OffenceFlagHelper offenceFlagHelper;

    @InjectMocks
    private MultiAgencyPublicProtectionArrangementsFlagResolver resolver;

    @BeforeEach
    void enableFeatureFlag() {
        lenient().when(featureFlags.enableMultiAgencyPublicProtectionArrangements()).thenReturn(true);
    }

    @Test
    void buildMultiAgencyPublicProtectionArrangementsFlagsMap_queriesRepositoryWithAllOffenceCodesAcrossAllResults() {
        HearingEntity hearing1 = hearingWithOffenceCodes("defendant-id-1", "AB001");
        HearingEntity hearing2 = hearingWithOffenceCodes("defendant-id-2", "CD002");
        CourtCaseEntity case1 = hearing1.getCourtCase();
        CourtCaseEntity case2 = hearing2.getCourtCase();

        given(offenceMappaMappingRepository.findByOffenceCodeIn(anyCollection()))
            .willReturn(List.of(
                OffenceMappaMappingEntity.builder().offenceCode("AB001").multiAgencyPublicProtectionArrangementsFlag(true).build(),
                OffenceMappaMappingEntity.builder().offenceCode("CD002").multiAgencyPublicProtectionArrangementsFlag(false).build()
            ));

        var result = resolver.buildMultiAgencyPublicProtectionArrangementsFlagsMap(List.of(
            new Pair<>(case1, hearing1.getHearingDefendants().get(0).getDefendant()),
            new Pair<>(case2, hearing2.getHearingDefendants().get(0).getDefendant())
        ));

        assertThat(result).isEqualTo(Map.of("AB001", true, "CD002", false));
        verify(offenceMappaMappingRepository).findByOffenceCodeIn(Set.of("AB001", "CD002"));
    }

    @Test
    void buildMultiAgencyPublicProtectionArrangementsFlagsMap_returnsEmptyMapWhenNoOffenceCodes() {
        HearingEntity hearing = hearingWithNoOffenceCodes("defendant-id-1");

        given(offenceMappaMappingRepository.findByOffenceCodeIn(anyCollection())).willReturn(List.of());

        var result = resolver.buildMultiAgencyPublicProtectionArrangementsFlagsMap(List.of(
            new Pair<>(hearing.getCourtCase(), hearing.getHearingDefendants().get(0).getDefendant())
        ));

        assertThat(result).isEmpty();
    }

    @Test
    void resolveMultiAgencyPublicProtectionArrangementsFlag_returnsTrueWhenAnyOffenceCodeMatchesMultiAgencyPublicProtectionArrangementsFlag() {
        HearingEntity hearing = hearingWithOffenceCodes("defendant-id-1", "AB001");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = resolver.resolveMultiAgencyPublicProtectionArrangementsFlag(hearing.getCourtCase(), defendant, Map.of("AB001", true));

        assertThat(result).isTrue();
    }

    @Test
    void resolveMultiAgencyPublicProtectionArrangementsFlag_returnsFalseWhenNoOffenceCodesMatchMultiAgencyPublicProtectionArrangementsFlag() {
        HearingEntity hearing = hearingWithOffenceCodes("defendant-id-1", "AB001");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = resolver.resolveMultiAgencyPublicProtectionArrangementsFlag(hearing.getCourtCase(), defendant, Map.of("AB001", false));

        assertThat(result).isFalse();
    }

    @Test
    void resolveMultiAgencyPublicProtectionArrangementsFlag_returnsFalseWhenOffenceCodeNotInMap() {
        HearingEntity hearing = hearingWithOffenceCodes("defendant-id-1", "AB001");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = resolver.resolveMultiAgencyPublicProtectionArrangementsFlag(hearing.getCourtCase(), defendant, Map.of());

        assertThat(result).isFalse();
    }

    @Test
    void resolveMultiAgencyPublicProtectionArrangementsFlag_returnsNullWhenDefendantHasNoOffenceCodes() {
        HearingEntity hearing = hearingWithNoOffenceCodes("defendant-id-1");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = resolver.resolveMultiAgencyPublicProtectionArrangementsFlag(hearing.getCourtCase(), defendant, Map.of());

        assertThat(result).isNull();
    }

    @Test
    void resolveMultiAgencyPublicProtectionArrangementsFlag_returnsTrueWhenAtLeastOneOfMultipleOffenceCodesMatchesMultiAgencyPublicProtectionArrangementsFlag() {
        HearingEntity hearing = hearingWithOffenceCodes("defendant-id-1", "AB001", "CD002");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = resolver.resolveMultiAgencyPublicProtectionArrangementsFlag(hearing.getCourtCase(), defendant, Map.of("AB001", false, "CD002", true));

        assertThat(result).isTrue();
    }

    @Test
    void resolveMultiAgencyPublicProtectionArrangementsFlag_returnsFalseWhenFeatureFlagDisabled() {
        given(featureFlags.enableMultiAgencyPublicProtectionArrangements()).willReturn(false);
        HearingEntity hearing = hearingWithOffenceCodes("defendant-id-1", "AB001");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = resolver.resolveMultiAgencyPublicProtectionArrangementsFlag(hearing.getCourtCase(), defendant, Map.of("AB001", true));

        assertThat(result).isFalse();
    }

    @Test
    void resolveMultiAgencyPublicProtectionArrangementsFlag_returnsNullWhenHearingTypeIsNotEligible() {
        HearingEntity hearing = hearingWithOffenceCodesAndHearingType("defendant-id-1", "Unknown", "AB001");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = resolver.resolveMultiAgencyPublicProtectionArrangementsFlag(hearing.getCourtCase(), defendant, Map.of("AB001", true));

        assertThat(result).isNull();
    }

    @Test
    void resolveMultiAgencyPublicProtectionArrangementsFlag_returnsTrueWhenHearingTypeIsSentence() {
        HearingEntity hearing = hearingWithOffenceCodes("defendant-id-1", "AB001");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = resolver.resolveMultiAgencyPublicProtectionArrangementsFlag(hearing.getCourtCase(), defendant, Map.of("AB001", true));

        assertThat(result).isTrue();
    }

    @Test
    void resolveMultiAgencyPublicProtectionArrangementsFlag_returnsTrueWhenHearingTypeIsSentenceProsecutionToAttend() {
        HearingEntity hearing = hearingWithOffenceCodesAndHearingType("defendant-id-1", "Sentence (Prosecution to Attend)", "AB001");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = resolver.resolveMultiAgencyPublicProtectionArrangementsFlag(hearing.getCourtCase(), defendant, Map.of("AB001", true));

        assertThat(result).isTrue();
    }

    @Test
    void resolveMultiAgencyPublicProtectionArrangementsFlagFromHearing_returnsNullWhenHearingTypeIsNotEligible() {
        HearingEntity hearing = hearingWithOffenceCodesAndHearingType("defendant-id-1", "Unknown", "AB001");
        DefendantEntity defendant = hearing.getHearingDefendants().get(0).getDefendant();

        var result = resolver.resolveMultiAgencyPublicProtectionArrangementsFlagFromHearing(hearing, defendant, Map.of("AB001", true));

        assertThat(result).isNull();
    }

    @Test
    void buildMultiAgencyPublicProtectionArrangementsFlagsMapFromHearing_returnsEmptyMapWhenHearingTypeIsNotEligible() {
        HearingEntity hearing = hearingWithOffenceCodesAndHearingType("defendant-id-1", "Unknown", "AB001");

        var result = resolver.buildMultiAgencyPublicProtectionArrangementsFlagsMapFromHearing(hearing);

        assertThat(result).isEmpty();
    }

    private HearingEntity hearingWithNoOffenceCodes(String defendantId) {
        return buildHearing(defendantId, "sentence", List.of(OffenceEntity.builder().title("offence no code").build()));
    }

    private HearingEntity hearingWithOffenceCodes(String defendantId, String... offenceCodes) {
        return hearingWithOffenceCodesAndHearingType(defendantId, "sentence", offenceCodes);
    }

    private HearingEntity hearingWithOffenceCodesAndHearingType(String defendantId, String hearingType, String... offenceCodes) {
        List<OffenceEntity> offences = java.util.Arrays.stream(offenceCodes)
            .map(code -> OffenceEntity.builder().title("offence " + code).offenceCode(code).build())
            .collect(java.util.stream.Collectors.toList());
        return buildHearing(defendantId, hearingType, offences);
    }

    private HearingEntity buildHearing(String defendantId, String hearingType, List<OffenceEntity> offences) {
        HearingEntity hearing = EntityHelper.aHearingEntityWithCrnAndCaseIdAndHearingId("X123", "case-1", defendantId);
        var updatedDefendant = hearing.getHearingDefendants().get(0).withOffences(offences);
        var updatedHearing = hearing.withHearingDefendants(List.of(updatedDefendant)).withHearingType(hearingType);
        CourtCaseEntity courtCase = CourtCaseEntity.builder().hearings(new java.util.ArrayList<>()).build();
        var hearingWithCourtCase = updatedHearing.withCourtCase(courtCase);
        courtCase.addHearing(hearingWithCourtCase);
        EntityHelper.refreshMappings(hearingWithCourtCase);
        return hearingWithCourtCase;
    }
}
