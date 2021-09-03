package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SourceTypeTest {

    @Test
    void whenValueShouldBeCommonPlatform() {
        assertThat(SourceType.from("XXX")).isSameAs(SourceType.COMMON_PLATFORM);
        assertThat(SourceType.from(null)).isSameAs(SourceType.COMMON_PLATFORM);
        assertThat(SourceType.from("")).isSameAs(SourceType.COMMON_PLATFORM);
        assertThat(SourceType.from("libr")).isSameAs(SourceType.COMMON_PLATFORM);
        assertThat(SourceType.from("    common_platform")).isSameAs(SourceType.COMMON_PLATFORM);
    }

    @Test
    void whenValueShouldBeLibra() {
        assertThat(SourceType.from("LIBRA")).isSameAs(SourceType.LIBRA);
        assertThat(SourceType.from("lIBra")).isSameAs(SourceType.LIBRA);
        assertThat(SourceType.from("lIBra    ")).isSameAs(SourceType.LIBRA);
    }
}
