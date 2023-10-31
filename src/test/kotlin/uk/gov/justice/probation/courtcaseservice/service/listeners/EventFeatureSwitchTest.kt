package uk.gov.justice.probation.courtcaseservice.service.listeners

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.probation.courtcaseservice.BaseIntTest
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.EventFeatureSwitch

internal class EventFeatureSwitchTest : BaseIntTest() {

  @Autowired
  private lateinit var featureSwitch: EventFeatureSwitch

  @Test
  fun `should return true when feature is enabled`() {
    assertThat(featureSwitch.isEnabled("probation-case.engagement.created")).isTrue
  }
}
