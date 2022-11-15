package uk.gov.justice.probation.courtcaseservice.jpa;

import org.assertj.core.api.Assertions;
import org.hibernate.mapping.Table;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CCSSchemaFilterTest {

    final CCSSchemaFilter ccsSchemaFilter = new CCSSchemaFilter();

    @ParameterizedTest
    @CsvSource({
        "hearing,false",
        "hearing_aud, true",
        "hearing_AUD, true",
        "court_case,false",
        "court_case_aud, true",
        "REVINFO, true",
        "revinfo, true",
        "anythingelse, false",
        "anythingelse_aud, true",
    })
    void givenAuditTablesReturnTrue_otherwiseFalse(String tableName, boolean expected) {
        Assertions.assertThat(ccsSchemaFilter.includeTable(new Table(tableName))).isEqualTo(expected);
    }
}