package uk.gov.justice.probation.courtcaseservice.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FeatureFlagsTest {

    private FeatureFlags featureFlags;

    @BeforeEach
    void beforeEach() {
        this.featureFlags = new FeatureFlags();
    }

    @DisplayName("get toggle value default true")
    @Test
    void getSimpleFlagDefault() {
        assertThat(featureFlags.sentenceData()).isTrue();
    }

    @DisplayName("Set and get toggle value")
    @Test
    void testGetSimpleFlag() {
        featureFlags.setFlagValue("flag-test", false);

        assertThat(featureFlags.getFlags().get("flag-test")).isFalse();
    }
}
