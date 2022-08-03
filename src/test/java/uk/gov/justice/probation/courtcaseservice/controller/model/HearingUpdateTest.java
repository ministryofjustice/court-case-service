package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aHearingDayEntity;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aHearingDefendantEntity;

class HearingUpdateTest {

    @Test
    void shouldMapToHearingUpdate() {

        final var defendantId1 = "9f3da51a-2ad0-4648-8b71-79badf064fcf";
        final var defendantId2 = "c1f76bea-16f7-4574-bb8a-44875078bc0d";

        HearingDefendantEntity hd1 = aHearingDefendantEntity().withDefendantId(defendantId1)
            .withOffences(List.of(OffenceEntity.builder().title("offence 001").build(),
                OffenceEntity.builder().title("offence 002").build()));
        HearingDefendantEntity hd2 = aHearingDefendantEntity().withDefendantId(defendantId2)
            .withOffences(List.of(OffenceEntity.builder().title("offence 003").build()));

        final var testHearingEntity = EntityHelper
            .aHearingEntity("a465cfa0-e539-4958-88fc-6276207873ca")
            .withHearingDays(List.of(aHearingDayEntity(), aHearingDayEntity()))
            .withHearingDefendants(List.of(hd1, hd2));

        var actual = HearingUpdate.of(testHearingEntity);

        assertThat(actual.getCreated()).isEqualTo(testHearingEntity.getFirstCreated());
        assertThat(actual.getHearingDays()).hasSize(2);
        assertThat(actual.getDefendantIds()).isEqualTo(List.of(defendantId1, defendantId2));
        assertThat(actual.getOffences()).isEqualTo(Stream.concat(hd1.getOffences().stream(),
            hd2.getOffences().stream()).collect(Collectors.toList()));
    }
}