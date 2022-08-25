package uk.gov.justice.probation.courtcaseservice

import org.junit.ClassRule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.retry.annotation.EnableRetry
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.probation.courtcaseservice.controller.CCSPostgresqlContainer
import uk.gov.justice.probation.courtcaseservice.testcontainers.LocalStackHelper
import uk.gov.justice.probation.courtcaseservice.testcontainers.LocalStackHelper.setLocalStackProperties
import uk.gov.justice.probation.courtcaseservice.wiremock.WiremockExtension
import uk.gov.justice.probation.courtcaseservice.wiremock.WiremockMockServer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableRetry
abstract class BaseIntTest {
  @JvmField
  @LocalServerPort
  protected var port = 0
  @BeforeEach
  fun setup() {
    TestConfig.configureRestAssuredForIntTest(port)
  }
  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  @SpyBean
  protected lateinit var inboundMessageServiceSpy: HmppsQueueService

  companion object {
    private val localStackContainer = LocalStackHelper.instance

    @JvmStatic
    @DynamicPropertySource
    fun testcontainers(registry: DynamicPropertyRegistry) {
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }

    private val MOCK_SERVER = WiremockMockServer(TestConfig.WIREMOCK_PORT)

    @RegisterExtension
    var wiremockExtension = WiremockExtension(MOCK_SERVER)
    var localstackImage = DockerImageName.parse("localstack/localstack:1.17.3")

    @Container
    var postgresqlContainer: PostgreSQLContainer<*> = CCSPostgresqlContainer.getInstance()

    @JvmField
    @ClassRule
    var localstack = LocalStackContainer(localstackImage)
      .withServices(
        LocalStackContainer.Service.SNS,
        LocalStackContainer.EnabledService.named("events")
      )

    @BeforeAll
    @Throws(Exception::class)
    fun setupClass() {
      RetryService.tryWireMockStub()
      MOCK_SERVER.start()
    }

    @AfterAll
    fun afterAll() {
      MOCK_SERVER.stop()
    }
  }
}
