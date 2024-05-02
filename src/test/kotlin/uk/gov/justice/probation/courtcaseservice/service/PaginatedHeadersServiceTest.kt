package uk.gov.justice.probation.courtcaseservice.service

import net.javacrumbs.jsonunit.assertj.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders

@ExtendWith(MockitoExtension::class)
class PaginatedHeadersServiceTest {


    lateinit var paginatedHeadersService: PaginatedHeadersService
    @BeforeEach
    fun initTest() {
        paginatedHeadersService = PaginatedHeadersService()
    }

    @Test
    fun `given pagination data, it returns a HTTPHeader with custom pagination headers`(){

        val headers: HttpHeaders = paginatedHeadersService.getHeaders(1, 2, 2, 4)

        assertThat(headers["X-PAGINATION-CURRENT-PAGE"]).contains("1")
        assertThat(headers["X-PAGINATION-PAGE-SIZE"]).contains("2")
        assertThat(headers["X-PAGINATION-TOTAL-PAGES"]).contains("2")
        assertThat(headers["X-PAGINATION-TOTAL-RESULTS"]).contains("4")
    }
}