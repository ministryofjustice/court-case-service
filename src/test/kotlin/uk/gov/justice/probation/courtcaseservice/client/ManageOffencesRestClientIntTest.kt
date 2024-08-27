package uk.gov.justice.probation.courtcaseservice.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.probation.courtcaseservice.BaseIntTest
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalApiEntityNotFoundException
import uk.gov.justice.probation.courtcaseservice.client.manageoffences.ApiError

class ManageOffencesRestClientIntTest : BaseIntTest() {


    @Autowired
    lateinit var manageOffencesRestClient: ManageOffencesRestClient

    @Autowired
    lateinit var objectMapper: ObjectMapper


    @Test
    fun should_return_home_office_code_for_valid_cjs_code() {
        // Given
        val cjsCode = "RT88191"

        // When
        val homeOfficeOffenceCode = manageOffencesRestClient.getHomeOfficeOffenceCodeByCJSCode(cjsCode)

        // Then
        assertThat(homeOfficeOffenceCode).isEqualTo("RT88507")
    }

    @Test
    fun should_return_not_found_for_unknown_cjs_code() {
        // Given
        val cjsCode = "Nonsense"

        // When
        val exception = assertThrows(ExternalApiEntityNotFoundException::class.java) {
           manageOffencesRestClient.getHomeOfficeOffenceCodeByCJSCode(cjsCode)
        }

        val apiError = objectMapper.readValue(exception.message, ApiError::class.java)
        assertThat(apiError.status).isEqualTo(404)
        assertThat(apiError.userMessage).isEqualTo("Not found: No offence exists for the passed in offence code")
        assertThat(apiError.developerMessage).isEqualTo("No offence exists for the passed in offence code")
    }
}