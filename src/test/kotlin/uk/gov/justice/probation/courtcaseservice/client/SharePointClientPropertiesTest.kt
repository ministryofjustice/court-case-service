package uk.gov.justice.probation.courtcaseservice.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

class SharePointClientPropertiesTest {

  private val contextRunner = ApplicationContextRunner()
    .withUserConfiguration(SharePointClientTestConfiguration::class.java)
    .withPropertyValues(
      "bail-information-analytics-sharepoint.tenant-id=tenant-123",
      "bail-information-analytics-sharepoint.client-id=client-456",
      "bail-information-analytics-sharepoint.client-secret=secret-789",
      "bail-information-analytics-sharepoint.user-id=user-001",
      "bail-information-analytics-sharepoint.site-host=justiceuk.sharepoint.com",
      "bail-information-analytics-sharepoint.site-path=/sites/BISAnalystsTeam",
      "bail-information-analytics-sharepoint.upload-folder-path=Shared Documents/General/CP CSV/New CSV/DEV",
    )

  @Test
  fun `should bind bail information analytics sharepoint properties`() {
    contextRunner.run { context ->
      assertThat(context).hasNotFailed()
      val client = context.getBean(SharePointClient::class.java)
      assertThat(client).isNotNull
    }
  }
}

@Configuration
@Import(SharePointClient::class)
private class SharePointClientTestConfiguration
