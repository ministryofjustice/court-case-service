package uk.gov.justice.probation.courtcaseservice.pact;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
@Provider("court-case-service")
@PactBroker
@Import(TestSecurity.class)
class CourtCaseMatcherVerificationPactTest extends BaseIntTest {

    @MockBean
    private OffenderMatchService offenderMatchService;

    @MockBean
    private CourtCaseService courtCaseService;

    @BeforeEach
    void setupTestTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port, "/"));
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State({"a case does not exist for court B10JQ and case number 1600028914"})
    void putCase() {
        var courtCaseEntity = EntityHelper.aCourtCaseEntity("X340741", "1600028914");
        when(courtCaseService.createCase(eq("B10JQ"), eq("1600028914"), any(CourtCaseEntity.class))).thenReturn(Mono.just(courtCaseEntity));
    }

    @State({"a case does not exist with grouped offender matches"})
    void postGroupedOffenderMatches() {
        when(offenderMatchService.createOrUpdateGroupedMatches(eq("B10JQ"), eq("1600028913"), any(GroupedOffenderMatchesRequest.class)))
            .thenReturn(Mono.just(GroupedOffenderMatchesEntity.builder().id(1234L).build()));
    }

    @State({"a case exists for court B10JQ and case number 1600028913"})
    void getCourtCase() {
        var courtCaseEntity = CourtCaseEntity.builder()
            .courtCode("B10JQ")
            .breach(true)
            .caseNo("1600028913")
            .crn("X340741")
            .pnc("A/1234560BA")
            .preSentenceActivity(true)
            .previouslyKnownTerminationDate(LocalDate.of(2010, Month.JANUARY, 1))
            .probationStatus("Current")
            .suspendedSentenceOrder(true)
            .sessionStartTime(LocalDateTime.now())
            .defendantSex("M")
            .build();
        when(courtCaseService.getCaseByCaseNumber("B10JQ", "1600028913")).thenReturn(courtCaseEntity);
    }
}
