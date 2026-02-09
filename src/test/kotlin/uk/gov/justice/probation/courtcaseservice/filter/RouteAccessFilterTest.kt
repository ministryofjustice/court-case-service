package uk.gov.justice.probation.courtcaseservice.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.core.env.Environment
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
internal class RouteAccessFilterTest {

  @Test
  fun `db-seed blocked when disabled`() {
    val environment = mock(Environment::class.java)
    given(environment.getProperty("db-seed.enabled", Boolean::class.javaObjectType, false)).willReturn(false)
    given(environment.activeProfiles).willReturn(arrayOf("dev"))
    val filter = RouteAccessFilter(environment)

    val request = MockHttpServletRequest().apply { requestURI = "/db-seed" }
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    assertThat(response.status).isEqualTo(HttpServletResponse.SC_FORBIDDEN)
    verify(chain, never()).doFilter(any(), any())
  }

  @Test
  fun `db-seed blocked when profile not allowed`() {
    val environment = mock(Environment::class.java)
    given(environment.getProperty("db-seed.enabled", Boolean::class.javaObjectType, false)).willReturn(true)
    given(environment.activeProfiles).willReturn(arrayOf("prod"))
    val filter = RouteAccessFilter(environment)

    val request = MockHttpServletRequest().apply { requestURI = "/db-seed" }
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    assertThat(response.status).isEqualTo(HttpServletResponse.SC_FORBIDDEN)
    verify(chain, never()).doFilter(any(), any())
  }

  @Test
  fun `db-seed allowed when enabled and profile permitted`() {
    val environment = mock(Environment::class.java)
    given(environment.getProperty("db-seed.enabled", Boolean::class.javaObjectType, false)).willReturn(true)
    given(environment.activeProfiles).willReturn(arrayOf("dev"))
    val filter = RouteAccessFilter(environment)

    val request = MockHttpServletRequest().apply { requestURI = "/db-seed" }
    val response = MockHttpServletResponse()
    val chain = mock(FilterChain::class.java)

    filter.doFilter(request, response, chain)

    assertThat(response.status).isEqualTo(HttpServletResponse.SC_OK)
    verify(chain, times(1)).doFilter(any(), any())
  }
}
