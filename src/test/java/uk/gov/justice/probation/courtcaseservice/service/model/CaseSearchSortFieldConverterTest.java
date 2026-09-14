package uk.gov.justice.probation.courtcaseservice.service.model;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaseSearchSortFieldConverterTest {

    private final CaseSearchSortFieldConverter subject = new CaseSearchSortFieldConverter();

    @Test
    void shouldMapStringToCaseSearchSortFieldConstant() {
        assertThat(subject.convert("nextHearingDate")).isEqualTo(CaseSearchSortFields.NEXT_HEARING_DATE);
    }

    @Test
    void shouldThrowBadRequestWhenUnableToConvertStringToEnum() {
        assertThrows(
            ResponseStatusException.class,
            () -> subject.convert("XXXXXXX"),
            "Invalid sort field \"XXXXXXX\""
        );
    }
}
