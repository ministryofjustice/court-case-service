package uk.gov.justice.probation.courtcaseservice

import org.awaitility.kotlin.atLeast
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.ClassRule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.retry.annotation.EnableRetry
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest
import software.amazon.awssdk.services.sqs.model.QueueAttributeName
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.probation.courtcaseservice.controller.CCSPostgresqlContainer
import uk.gov.justice.probation.courtcaseservice.testcontainers.LocalStackHelper
import uk.gov.justice.probation.courtcaseservice.testcontainers.LocalStackHelper.setLocalStackProperties
import uk.gov.justice.probation.courtcaseservice.wiremock.WiremockExtension
import uk.gov.justice.probation.courtcaseservice.wiremock.WiremockMockServer
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableRetry
abstract class BaseIntTest {

  @JvmField
  @LocalServerPort
  protected final var port = 0

  @BeforeEach
  fun setup() {
    TestConfig.configureRestAssuredForIntTest(port)
  }

  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  @SpyBean
  protected lateinit var inboundMessageServiceSpy: HmppsQueueService

  //Topic
  protected val offenderEventTopic by lazy { hmppsQueueService.findByTopicId("probationoffenderevents") ?: throw MissingQueueException("probationoffenderevents topic not found") }

  internal val hmppsDomainEvents by lazy { hmppsQueueService.findByTopicId("hmppsdomainevents") as HmppsTopic }

  internal val domainEventsTopic by lazy { hmppsQueueService.findByTopicId("domainevents") as HmppsTopic }
  internal val domainEventsTopicArn by lazy { domainEventsTopic.arn }


  //Queue
  private val emittedEventsQueue by lazy { hmppsQueueService.findByQueueId("emittedeventsqueue") ?: throw MissingQueueException("HmppsQueue emittedeventsqueue not found") }

  protected val emittedEventsQueueSqsClient by lazy { emittedEventsQueue.sqsClient }

  protected val emittedEventsQueueUrl by lazy { emittedEventsQueue.queueUrl }

  protected val offenderEventReceiverQueue by lazy { hmppsQueueService.findByQueueId("picprobationoffendereventsqueue") ?: throw MissingQueueException("picprobationoffendereventsqueue not found") }
  protected val offenderEventReceiverQueueSqsClient by lazy { offenderEventReceiverQueue.sqsClient }
  protected val offenderEventReceiverQueueUrl by lazy { offenderEventReceiverQueue.queueUrl }

  protected val newOffenderEventReceiverQueue by lazy { hmppsQueueService.findByQueueId("picnewoffendereventsqueue") ?: throw MissingQueueException("picprobationoffendereventsqueue not found") }
  protected val newOffenderEventReceiverQueueSqsClient by lazy { newOffenderEventReceiverQueue.sqsClient }
  protected val newOffenderEventReceiverQueueQueueUrl by lazy { newOffenderEventReceiverQueue.queueUrl }

  private fun SqsAsyncClient.countMessagesOnQueue(queueUrl: String, queueAttribute: QueueAttributeName): Int {

    val queueAttributesResult = this.getQueueAttributes(GetQueueAttributesRequest.builder()
      .queueUrl(queueUrl)
      .attributeNames(queueAttribute)
      .build())

    return queueAttributesResult.let {
      it.get().attributes()[queueAttribute]?.toInt() ?: 0
    }
  }

  fun assertOffenderEventReceiverQueueHasProcessedMessages() {
    // ApproximateNumberOfMessagesNotVisible represents messages in flight. So for this case if this is 1 means the message has been consumed but still not deleted until then the value will be 1 and ApproximateNumberOfMessages is zero as the message is inflight.
    // We need to ensure the inflight message is processed before checking for ApproximateNumberOfMessages.
    await atLeast AWAITILITY_DURATION untilCallTo { offenderEventReceiverQueueSqsClient.countMessagesOnQueue(offenderEventReceiverQueueUrl, QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE) } matches { it == 0 }
    await atLeast AWAITILITY_DURATION untilCallTo { offenderEventReceiverQueueSqsClient.countMessagesOnQueue(offenderEventReceiverQueueUrl, QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES) } matches { it == 0 }
  }

  fun assertNewOffenderDomainEventReceiverQueueHasProcessedMessages() {
    await atLeast AWAITILITY_DURATION untilCallTo { newOffenderEventReceiverQueueSqsClient.countMessagesOnQueue(newOffenderEventReceiverQueueQueueUrl, QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE) } matches { it == 0 }
    await atLeast AWAITILITY_DURATION untilCallTo { newOffenderEventReceiverQueueSqsClient.countMessagesOnQueue(newOffenderEventReceiverQueueQueueUrl, QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES) } matches { it == 0 }
  }


  companion object {
    private val localStackContainer = LocalStackHelper.instance

    protected val AWAITILITY_DURATION = Duration.ofSeconds(20)

    @JvmStatic
    @DynamicPropertySource
    fun testcontainers(registry: DynamicPropertyRegistry) {
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }

    public val WIRE_MOCK_SERVER = WiremockMockServer( TestConfig.WIREMOCK_PORT)

    @RegisterExtension
    var wiremockExtension = WiremockExtension(WIRE_MOCK_SERVER)
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
      WIRE_MOCK_SERVER.start()
    }

    @AfterAll
    fun afterAll() {
      WIRE_MOCK_SERVER.stop()
    }
  }
}
