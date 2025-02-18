package uk.gov.justice.probation.courtcaseservice

import org.awaitility.kotlin.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.retry.annotation.EnableRetry
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest
import software.amazon.awssdk.services.sqs.model.QueueAttributeName
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.probation.courtcaseservice.security.AuthAwareTokenConverter
import uk.gov.justice.probation.courtcaseservice.wiremock.WiremockExtension
import uk.gov.justice.probation.courtcaseservice.wiremock.WiremockMockServer
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableRetry
@Import(BaseIntTest.OverrideConfiguration::class)
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

  @MockitoSpyBean
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

  fun assertMessagesOnEmittedEventsQueue() {
    await atMost AWAITILITY_DURATION untilCallTo { emittedEventsQueueSqsClient.countMessagesOnQueue(emittedEventsQueueUrl, QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES) } matches { it == 2 }

  }

  fun assertOffenderEventReceiverQueueHasProcessedMessages() {
    // ApproximateNumberOfMessagesNotVisible represents messages in flight. So for this case if this is 1 means the message has been consumed but still not deleted until then the value will be 1 and ApproximateNumberOfMessages is zero as the message is inflight.
    // We need to ensure the inflight message is processed before checking for ApproximateNumberOfMessages.
    await atMost AWAITILITY_DURATION untilCallTo { offenderEventReceiverQueueSqsClient.countMessagesOnQueue(offenderEventReceiverQueueUrl, QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE) } matches { it == 0 }
    await atMost AWAITILITY_DURATION untilCallTo { offenderEventReceiverQueueSqsClient.countMessagesOnQueue(offenderEventReceiverQueueUrl, QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES) } matches { it == 0 }
  }

  fun assertNewOffenderDomainEventReceiverQueueHasProcessedMessages() {
    await atMost AWAITILITY_DURATION untilCallTo { newOffenderEventReceiverQueueSqsClient.countMessagesOnQueue(newOffenderEventReceiverQueueQueueUrl, QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE) } matches { it == 0 }
    await atMost AWAITILITY_DURATION untilCallTo { newOffenderEventReceiverQueueSqsClient.countMessagesOnQueue(newOffenderEventReceiverQueueQueueUrl, QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES) } matches { it == 0 }
  }


  companion object {

    protected val AWAITILITY_DURATION = Duration.ofSeconds(20)

    val WIRE_MOCK_SERVER = WiremockMockServer( TestConfig.WIREMOCK_PORT)

    @RegisterExtension
    var wiremockExtension = WiremockExtension(WIRE_MOCK_SERVER)

      @JvmStatic
      @BeforeAll
      @Throws(Exception::class)
      fun setupClass(): Unit {
          RetryService.tryWireMockStub()
          WIRE_MOCK_SERVER.start()
      }

      @JvmStatic
      @AfterAll
      fun afterAll(): Unit {
          WIRE_MOCK_SERVER.stop()
      }
  }

  @TestConfiguration
  @EnableWebSecurity
  class OverrideConfiguration {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
      // Usage of securityMatcher to replace the multiTenantHeaderFilterChain bean
      http.securityMatcher("/**")
      return http.csrf { it.disable() }
        .sessionManagement{ it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)}
        .oauth2Client {}
        .authorizeHttpRequests(
          Customizer { auth ->
          auth
            .requestMatchers(
              "/health/**",
              "/info",
              "/ping",
              "/swagger-ui.html",
              "/swagger-ui/**",
              "/v3/api-docs/**",
              "/queue-admin/retry-all-dlqs",
              "/process-un-resulted-cases"
            ).permitAll()
            .anyRequest()
            .hasAnyRole("PREPARE_A_CASE", "SAR_DATA_ACCESS")
        })
        .oauth2ResourceServer { it ->
          it.jwt{
            it.jwtAuthenticationConverter(AuthAwareTokenConverter())
          }
        }.build()
    }
  }
}
