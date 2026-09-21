package uk.gov.justice.probation.courtcaseservice.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.env.Environment
import org.springframework.core.env.getProperty
import org.springframework.web.filter.OncePerRequestFilter

class RouteAccessFilter(
  private val environment: Environment,
) : OncePerRequestFilter() {

  private val seedEndpoints = setOf("/db-seed", "/db-seed/scenario")
  private val seedConfigKey = "db-seed.enabled"
  private val allowedSeedingProfiles: List<String> = listOf("local", "dev")

  override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
    if (req.requestURI in seedEndpoints) {
      if (!dbSeederEnabled() || !dbSeederAllowed()) {
        res.sendError(HttpServletResponse.SC_FORBIDDEN)
        return
      }
    }
    chain.doFilter(req, res)
  }

  private fun dbSeederEnabled(): Boolean = environment.getProperty<Boolean>(seedConfigKey, false)

  private fun dbSeederAllowed(): Boolean = environment.activeProfiles.any { it in allowedSeedingProfiles }
}
