package uk.gov.justice.probation.courtcaseservice.application

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.retry.support.RetryTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.probation.courtcaseservice.TestAppender

@ExtendWith(SpringExtension::class)
class ApplicationRetryListenerTest {

    @Test
    fun givenTemplateRetryService_whenCallWithException_thenRetry() {

        TestAppender.events.clear()
        val retryTemplate = RetryTemplate()
        retryTemplate.registerListener(ApplicationRetryListener())

        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy {
                retryTemplate.execute<Any?, RuntimeException> {
                    throw RuntimeException("test")
                }
            }
            .withMessage("test")

        val events = TestAppender.events.stream()
            .map { it.formattedMessage }

        assertThat(events).contains(
            "Operation failed with exception. Attempting retry 1",
            "Retry attempt 1 failed with error",
            "Retry attempt 2 failed with error",
            "Retry attempt 3 failed with error",
            "Retried 3 times"
        )
    }
}
