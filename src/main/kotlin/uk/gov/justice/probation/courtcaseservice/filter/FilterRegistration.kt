package uk.gov.justice.probation.courtcaseservice.filter

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.env.Environment

@Bean
fun routeAccessFilter(environment: Environment): FilterRegistrationBean<RouteAccessFilter> =
  FilterRegistrationBean(RouteAccessFilter(environment)).apply {
    addUrlPatterns("/db-seed")           // restrict specific endpoint
    order = Ordered.HIGHEST_PRECEDENCE   // ensure it runs early
  }
