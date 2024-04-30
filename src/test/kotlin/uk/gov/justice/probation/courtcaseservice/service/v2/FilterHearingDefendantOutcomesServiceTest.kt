package uk.gov.justice.probation.courtcaseservice.service.v2

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.probation.courtcaseservice.controller.CaseWorkflowControllerTest
import uk.gov.justice.probation.courtcaseservice.controller.model.filters.FilterItem
import uk.gov.justice.probation.courtcaseservice.controller.model.filters.FilteredHearingDefendantOutcomesResponse
import uk.gov.justice.probation.courtcaseservice.controller.model.filters.FiltersList
import uk.gov.justice.probation.courtcaseservice.controller.model.filters.HearingOutcomeStatesFilterItem
import uk.gov.justice.probation.courtcaseservice.controller.model.v2.HearingOutcomeCountByState
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeAssignedUser
import uk.gov.justice.probation.courtcaseservice.controller.model.v2.HearingOutcomeCaseList as V2HearingOutcomeCaseList

@ExtendWith(MockitoExtension::class)
class FilterHearingDefendantOutcomesServiceTest {

    @Mock
    lateinit var hearingDefendantOutcomesCaseList: V2HearingOutcomeCaseList

    private lateinit var service: FilterHearingDefendantOutcomesService
    @BeforeEach
    fun initTest() {
        hearingDefendantOutcomesCaseList =
            V2HearingOutcomeCaseList(
                listOf(),
                HearingOutcomeCountByState(listOf(Pair("NEW", 1), Pair("IN_PROGRESS",0), Pair("Resulted", 2))),
                CaseWorkflowControllerTest.TEST_COURT_ROOMS,
                1,
                1,
                1,
                listOf<HearingOutcomeAssignedUser>(HearingOutcomeAssignedUser("John Doe", "UUID"))
            )
        service = FilterHearingDefendantOutcomesService(hearingDefendantOutcomesCaseList)
    }

    @Test
    fun `getResponse provides the filters available for hearing defendant outcomes`(){
        val result: FilteredHearingDefendantOutcomesResponse = service.getResponse()

        assertThat(result).usingRecursiveComparison().isEqualTo(FilteredHearingDefendantOutcomesResponse(hearingDefendantOutcomesCaseList.records,
            mutableListOf(
                FiltersList("assignedUsers", "Assigned Users", true, listOf(FilterItem("UUID", "John Doe", true))),
                FiltersList("courtRooms", "Court Rooms", true, listOf(FilterItem("01", "01", true), FilterItem("Court room - 2", "Court room - 2", true))),
                FiltersList("states", "Hearing Outcome States", false, listOf(HearingOutcomeStatesFilterItem("NEW", "New", 1, true ), HearingOutcomeStatesFilterItem("IN_PROGRESS", "In Progress", 0, true ), HearingOutcomeStatesFilterItem("RESULTED", "Resulted", 0, true)))
            )
        ))
    }
}