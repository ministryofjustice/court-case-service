package uk.gov.justice.probation.courtcaseservice.service

import org.springframework.http.HttpHeaders

class PaginatedHeadersService {

    fun getHeaders(page: Int, size: Int, totalPages: Int, totalElements: Int): HttpHeaders {
        val responseHeaders = HttpHeaders()
        responseHeaders.set("X-PAGINATION-CURRENT-PAGE", java.lang.String.valueOf(page))
        responseHeaders.set("X-PAGINATION-PAGE-SIZE", java.lang.String.valueOf(size))
        responseHeaders.set("X-PAGINATION-TOTAL-PAGES", java.lang.String.valueOf(totalPages))
        responseHeaders.set("X-PAGINATION-TOTAL-RESULTS", java.lang.String.valueOf(totalElements))

        return responseHeaders
    }
}