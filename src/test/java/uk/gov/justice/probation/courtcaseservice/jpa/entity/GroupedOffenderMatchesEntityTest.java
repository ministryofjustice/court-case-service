package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GroupedOffenderMatchesEntityTest {

    @Test
    void shouldUpdateExistingMatchesAndAddNewAndRemoveNonExistingMatches() {
        OffenderMatchEntity matchRetainedDuringAUpdate = OffenderMatchEntity.builder().crn("crn1").cro("cro1").createdBy("u1").id(1L).build();
        OffenderMatchEntity matchToBeUpdated = OffenderMatchEntity.builder().crn("crn2").cro("cro2").createdBy("u2").id(2L).build();
        OffenderMatchEntity matchToBeDeletedOnUpdate = OffenderMatchEntity.builder().crn("crn5").cro("cro5").createdBy("u5").id(2L).build();
        var existingMatches =  EntityHelper.getMutableList((List.of(matchRetainedDuringAUpdate, matchToBeUpdated, matchToBeDeletedOnUpdate)));

        var group = GroupedOffenderMatchesEntity.builder().caseId("case-id").defendantId("defendant-id").offenderMatches(existingMatches).build();
        matchRetainedDuringAUpdate.setGroup(group);
        matchToBeUpdated.setGroup(group);
        matchToBeDeletedOnUpdate.setGroup(group);

        OffenderMatchEntity brandNewMatch = OffenderMatchEntity.builder().crn("crn3").cro("cro3").createdBy("u3").build();
        OffenderMatchEntity match2Update = OffenderMatchEntity.builder().crn("crn2").cro("newcro2").createdBy("u2").build();
        var newMatches = List.of(matchRetainedDuringAUpdate, brandNewMatch, match2Update);

        group.updateMatches(newMatches);
        assertThat(group.getOffenderMatches(), Matchers.hasSize(3));

        List<OffenderMatchEntity> expected = EntityHelper.getMutableList(List.of(matchRetainedDuringAUpdate,
            OffenderMatchEntity.builder().crn("crn2").createdBy("u2").cro("newcro2").group(group).id(2L).build(),
            OffenderMatchEntity.builder().crn("crn3").cro("cro3").createdBy("u3").group(group).build()));

        assertThat(group.getOffenderMatches(), is(expected));
    }
}
