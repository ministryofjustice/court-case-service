package uk.gov.justice.probation.courtcaseservice.testUtil


import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource
import org.springframework.boot.builder.SpringApplicationBuilder
import uk.gov.justice.probation.courtcaseservice.CourtCaseServiceApplication

class MultiApplicationContextExtension : BeforeAllCallback, CloseableResource {

    private val instance1: SpringApplicationBuilder = SpringApplicationBuilder(CourtCaseServiceApplication::class.java)
            .profiles("test", "test-instance-1")

    override fun beforeAll(context: ExtensionContext) {
        if (!started) {
            started = true
            instance1.run()

            context.root.getStore(ExtensionContext.Namespace.GLOBAL).put(
                    "test-instance-extension",
                    this,
            )
        }
    }

    override fun close() {
        instance1.context().close()
    }

    companion object {
        private var started = false
    }
}