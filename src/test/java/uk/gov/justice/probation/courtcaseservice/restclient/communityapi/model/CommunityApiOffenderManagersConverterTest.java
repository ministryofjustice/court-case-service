package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CommunityApiOffenderManagersConverterTest {

    @Test
    public void shouldIgnoreUnknownValues() {
        final var communityApiOffenderManagersConverter = new CommunityApiOffenderManagersConverter();
        final List<Map<String, Object>> maps = List.of(
                Map.of(
                        "staffCode", "staff code",
                        "unknownValue", "unknown value"
                )
        );
        final var actual = communityApiOffenderManagersConverter.convert(maps);

        assertThat(actual.getOffenderManagers().get(0).getStaffCode()).isEqualTo("staff code");
    }

}
