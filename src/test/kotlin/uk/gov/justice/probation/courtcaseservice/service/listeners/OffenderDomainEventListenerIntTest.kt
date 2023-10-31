package uk.gov.justice.probation.courtcaseservice.service.listeners


import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.justice.probation.courtcaseservice.BaseIntTest
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.DomainEvent
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.PersonIdentifier
import uk.gov.justice.probation.courtcaseservice.client.model.listeners.PersonReference
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository
import uk.gov.justice.probation.courtcaseservice.service.listeners.notifiers.NEW_OFFENDER_CREATED
import java.time.LocalDate
import java.util.concurrent.CompletableFuture

@Sql(
    scripts = ["classpath:before-test.sql"],
    config = SqlConfig(transactionMode = TransactionMode.ISOLATED)
)
@Sql(
    scripts = ["classpath:after-test.sql"],
    config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD
)
class OffenderDomainEventListenerIntTest : BaseIntTest() {

    @Autowired
    lateinit var defendantRepository: DefendantRepository

    @Autowired
    lateinit var offenderRepository: OffenderRepository

    @Autowired
    lateinit var hearingRepository: HearingRepository

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        newOffenderEventReceiverQueueSqsClient.purgeQueue(
            PurgeQueueRequest.builder().queueUrl(newOffenderEventReceiverQueueQueueUrl).build()
        )
    }

    @Test
    fun `should create a new offender and link all matching defendants when new offender event published`() {
        // Given
        val defendantsBeforeEvent = defendantRepository.findDefendantsByOffenderCrn("XXX1234")
        assertThat(defendantsBeforeEvent).isEmpty()

        val expectedMatchingDefendantToBeUpdated =
            defendantRepository.findMatchingDefendants("PN/1234560XX", LocalDate.of(1939, 10, 10), "David", "BOWIE")
        assertThat(expectedMatchingDefendantToBeUpdated).isNotEmpty()
        assertThat(expectedMatchingDefendantToBeUpdated.size).isEqualTo(2)

        val offenderEntityBeforeEvent = offenderRepository.findByCrn("XXX1234")
        assertThat(offenderEntityBeforeEvent).isEmpty

        val domainEvent = objectMapper.writeValueAsString(createDomainEvent(NEW_OFFENDER_CREATED, "XXX1234"))

        val publishRequest = PublishRequest.builder().topicArn(domainEventsTopicArn)
            .message(domainEvent)
            .messageAttributes(
                mapOf(
                    "eventType" to MessageAttributeValue.builder().dataType("String")
                        .stringValue(NEW_OFFENDER_CREATED).build(),
                ),
            ).build()

        // When publish new offender domain event with CRN XXX1234
        publishOffenderEvent(publishRequest)

        //Then
        assertNewOffenderDomainEventReceiverQueueHasProcessedMessages()

        val offenderEntityAfterEvent = offenderRepository.findByCrn("XXX1234")
        assertThat(offenderEntityAfterEvent.isPresent).isTrue()

        val defendantsAfterTheEvent = defendantRepository.findDefendantsByOffenderCrn("XXX1234")
        assertThat(defendantsAfterTheEvent).isNotEmpty
        assertThat(defendantsAfterTheEvent.size).isEqualTo(2)

        assertThat(defendantsAfterTheEvent).usingRecursiveComparison()
            .comparingOnlyFields("defendantId")
            .isEqualTo(expectedMatchingDefendantToBeUpdated)
    }

    fun publishOffenderEvent(publishRequest: PublishRequest): CompletableFuture<PublishResponse>? {
        return domainEventsTopic.snsClient.publish(publishRequest)
    }

    fun createDomainEvent(eventType: String, crn: String): DomainEvent {
        val crnType = PersonIdentifier("CRN", crn)
        val personReference = PersonReference(listOf(crnType))
        return DomainEvent(eventType = eventType, detailUrl = createDetailUrl(crn), personReference = personReference)
    }

    fun createDetailUrl(crn: String): String {
        val builder = StringBuilder()
        builder.append("/probation-case.engagement.created/")
        builder.append(crn)
        return builder.toString()
    }
}