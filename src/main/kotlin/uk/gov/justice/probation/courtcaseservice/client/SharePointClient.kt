package uk.gov.justice.probation.courtcaseservice.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

@Component
class SharePointClient(
  @param:Value("\${bail-information-analytics-sharepoint.tenant-id:}") private val tenantId: String,
  @param:Value("\${bail-information-analytics-sharepoint.client-id:}") private val clientId: String,
  @param:Value("\${bail-information-analytics-sharepoint.client-secret:}") private val clientSecret: String,
  @param:Value("\${bail-information-analytics-sharepoint.user-id:}") private val userId: String,
  @param:Value("\${bail-information-analytics-sharepoint.site-host:}") private val siteHost: String,
  @param:Value("\${bail-information-analytics-sharepoint.site-path:}") private val sitePath: String,
  @param:Value("\${bail-information-analytics-sharepoint.upload-folder-path:}") private val uploadFolderPath: String,
) {

  companion object {
    private val log = LoggerFactory.getLogger(SharePointClient::class.java)
    private const val GRAPH_BASE_URL = "https://graph.microsoft.com/v1.0"
    private const val GRAPH_SCOPE = "https://graph.microsoft.com/.default"
  }

  fun uploadFile(fileName: String, content: ByteArray) {
    log.info("Acquiring Microsoft Graph access token for SharePoint upload")
    val token = acquireAccessToken()
    val encodedFolderPath = uploadFolderPath.replace(" ", "%20")
    val encodedFileName = fileName.replace(" ", "%20")

    val uploadUrl = if (siteHost.isNotBlank() && sitePath.isNotBlank()) {
      val encodedSitePath = sitePath.replace(" ", "%20")
      "$GRAPH_BASE_URL/sites/$siteHost:$encodedSitePath:/drive/root:/$encodedFolderPath/$encodedFileName:/content"
    } else {
      require(userId.isNotBlank()) { "sharepoint.user-id must be configured when site-host/site-path are not set" }
      "$GRAPH_BASE_URL/users/$userId/drive/root:/$encodedFolderPath/$encodedFileName:/content"
    }
    log.info("Uploading {} to SharePoint path: {}/{}", fileName, uploadFolderPath, fileName)

    WebClient.create()
      .put()
      .uri(uploadUrl)
      .header("Authorization", "Bearer $token")
      .contentType(MediaType.parseMediaType("text/csv"))
      .bodyValue(content)
      .retrieve()
      .toBodilessEntity()
      .block()

    log.info("Successfully uploaded {} to SharePoint", fileName)
  }

  private fun acquireAccessToken(): String {
    val tokenUrl = "https://login.microsoftonline.com/$tenantId/oauth2/v2.0/token"

    val formData = LinkedMultiValueMap<String, String>().apply {
      add("grant_type", "client_credentials")
      add("client_id", clientId)
      add("client_secret", clientSecret)
      add("scope", GRAPH_SCOPE)
    }

    val response = WebClient.create()
      .post()
      .uri(tokenUrl)
      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .body(BodyInserters.fromFormData(formData))
      .retrieve()
      .bodyToMono(Map::class.java)
      .block()

    return response?.get("access_token") as? String
      ?: throw IllegalStateException("Failed to acquire Microsoft Graph access token")
  }
}
