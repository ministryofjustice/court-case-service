package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DefendantTest {

    @Test
    void givenNullSex_whenGet_thenReturnNotKnown() {
        // Consumers require "M", "F", "N", "NS" etc, via PACTs
        assertThat(Defendant.builder().build().getSex()).isEqualTo("N");
        assertThat(Defendant.builder().sex("   ").build().getSex()).isEqualTo("N");
    }

    @Test
    void givenFemaleSex_whenGet_thenReturnM() {
        assertThat(Defendant.builder().sex("female").build().getSex()).isEqualTo("F");
        assertThat(Defendant.builder().sex("FEMALE").build().getSex()).isEqualTo("F");
        assertThat(Defendant.builder().sex("F").build().getSex()).isEqualTo("F");
    }

    @Test
    void givenMaleSex_whenGet_thenReturnF() {
        assertThat(Defendant.builder().sex("maLE").build().getSex()).isEqualTo("M");
        assertThat(Defendant.builder().sex("MALE").build().getSex()).isEqualTo("M");
        assertThat(Defendant.builder().sex("M").build().getSex()).isEqualTo("M");
    }

    @Test
    void givenNotKnownSex_whenGet_thenReturnN() {
        assertThat(Defendant.builder().sex("NOT_KNOWN").build().getSex()).isEqualTo("N");
        assertThat(Defendant.builder().sex("N").build().getSex()).isEqualTo("N");
    }

    @Test
    void givenNotSpecifiedSex_whenGet_thenReturnNS() {
        assertThat(Defendant.builder().sex("NOT_SPECIFIED").build().getSex()).isEqualTo("NS");
        assertThat(Defendant.builder().sex("NS").build().getSex()).isEqualTo("NS");
    }
    @Test
    void givenNullPersonId_whenGet_thenReturnWithPersonId(){
        assertThat(Defendant.builder().personId(null).build().getPersonId()).isNotBlank();
    }
    @Test
    void givenPersonId_whenGet_thenReturnSamePersonId(){
        assertThat(Defendant.builder().personId("1").build().getPersonId()).isEqualTo("1");
    }

}
