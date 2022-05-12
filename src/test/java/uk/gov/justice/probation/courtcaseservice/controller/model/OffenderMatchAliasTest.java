package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OffenderMatchAliasTest {

    @Test
    void shouldMapToEntity() {
        final var dateOfBirth = LocalDate.now();
        final var offenderAliasDto = OffenderMatchAlias.builder()
            .dateOfBirth(dateOfBirth)
            .firstName("firstNameOne")
            .middleNames(List.of("middleOne", "middleTwo"))
            .surname("surnameOne")
            .gender("Not Specified")
            .build();
        var actual = offenderAliasDto.asEntity();
        assertThat(actual.getDateOfBirth()).isEqualTo(dateOfBirth);
        assertThat(actual.getFirstName()).isEqualTo(offenderAliasDto.getFirstName());
        assertThat(actual.getMiddleNames()).isEqualTo(offenderAliasDto.getMiddleNames());
        assertThat(actual.getSurname()).isEqualTo(offenderAliasDto.getSurname());
        assertThat(actual.getGender()).isEqualTo(offenderAliasDto.getGender());
    }
}