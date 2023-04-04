package uk.gov.justice.probation.courtcaseservice.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CaseSearchTypeResolverTest {

    @ParameterizedTest
    @CsvSource({
        "C123456, C123456, CRN",
        "CB23456, CB23456, NAME",
        "1234567, 1234567, NAME",
        "C123, C123, NAME",
        "Jeff, Jeff, NAME",
        "Jeff Blogs, Jeff & Blogs, NAME",
        "Jeff Mid Blogs,Jeff & Mid & Blogs, NAME",
        "Jeff 123 Blogs,Jeff & 123 & Blogs, NAME"
    })
    void givenTHeSearchTerm_shouldResolveToAppropriateSearchResolver(String input, String expectedSearchTerm, CaseSearchType searchType) {
        var resolver = CaseSearchTypeResolver.get(input);
        Assertions.assertThat(resolver.getType()).isEqualTo(searchType);
        Assertions.assertThat(resolver.getSearchTerm()).isEqualTo(input);
        Assertions.assertThat(resolver.getExtendedSearchTerm()).isEqualTo(expectedSearchTerm);
    }
}