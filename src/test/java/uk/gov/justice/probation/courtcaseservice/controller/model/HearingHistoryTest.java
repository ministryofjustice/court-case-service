package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aHearingDayEntity;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aHearingDefendantEntity;

class HearingHistoryTest {

    @Test
    void shouldMapHearingHistoryCorrectly() {
        final var testHearingId = "2195e7c1-1696-4bb8-9300-f303d3d60166";
        final var testHearingEntity = EntityHelper
            .aHearingEntity("a465cfa0-e539-4958-88fc-6276207873ca")
            .withHearingDays(List.of(aHearingDayEntity(), aHearingDayEntity()))
            .withHearingDefendants(List.of(aHearingDefendantEntity(), aHearingDefendantEntity()));

        var actual = HearingHistory.of(testHearingId, List.of(testHearingEntity, testHearingEntity, testHearingEntity));

        assertThat(actual.getHearingId()).isEqualTo(testHearingId);
        assertThat(actual.getHearingUpdates()).hasSize(3);
    }
}
