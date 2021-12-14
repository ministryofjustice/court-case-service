package uk.gov.justice.probation.courtcaseservice.application

import org.slf4j.LoggerFactory
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.stereotype.Component

@Component
class ApplicationRetryListener : RetryListener {
    companion object {
        private val log = LoggerFactory.getLogger(ApplicationRetryListener::class.java)
    }

    override fun <T : Any?, E : Throwable?> open(context: RetryContext?, callback: RetryCallback<T, E>?): Boolean {
        log.warn("Operation failed with exception. Attempting retry {}", context?.retryCount?.plus(1))
        return true
    }

    override fun <T : Any?, E : Throwable?> onError(
        context: RetryContext?,
        callback: RetryCallback<T, E>?,
        throwable: Throwable?
    ) {
        log.warn("Retry attempt {} failed with error", context?.retryCount, context?.lastThrowable)
    }

    override fun <T : Any?, E : Throwable?> close(
        context: RetryContext?,
        callback: RetryCallback<T, E>?,
        throwable: Throwable?
    ) {
        log.warn("Retried {} times. Last attempt failed with error", context?.retryCount, context?.lastThrowable)
    }
}
