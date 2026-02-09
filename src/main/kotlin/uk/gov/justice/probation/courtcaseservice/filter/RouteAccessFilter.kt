package uk.gov.justice.probation.courtcaseservice.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.env.Environment
import org.springframework.core.env.getProperty
import org.springframework.web.filter.OncePerRequestFilter

class RouteAccessFilter(
  private val environment: Environment
) : OncePerRequestFilter() {

  private val seedEndpoint = "/db-seed"
  private val seedConfigKey = "db-seed.enabled"
  private val allowedSeedingProfiles: List<String> = listOf("local", "dev", "test")

  override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
    if (req.requestURI == seedEndpoint) {
      if (!dbSeederEnabled() || !dbSeederAllowed()) {
        res.sendError(HttpServletResponse.SC_FORBIDDEN)
        return
      }
    }
    chain.doFilter(req, res)
  }

  private fun dbSeederEnabled(): Boolean {
    return environment.getProperty<Boolean>(seedConfigKey, false)
  }

  private fun dbSeederAllowed(): Boolean {
    return environment.activeProfiles.any { it in allowedSeedingProfiles }
  }
}
