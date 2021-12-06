package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * See ADR 0009-handling-concurrent-db-updates.md for context
 */
public class OffenderMatchServiceIntTest extends BaseIntTest {
    @Autowired
    private OffenderMatchService offenderMatchService;
    @MockBean
    private GroupedOffenderMatchRepository offenderMatchRepository;

    @BeforeEach
    public void setUp() {
        // Set request context to satisfy @RequestScope
        final var request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void givenCannotAcquireLockExceptionThrown_whenCreateOrUpdateGroupedMatchesByDefendant_thenRetry() {
        when(offenderMatchRepository.findByCaseIdAndDefendantId(any(), any())).thenThrow(CannotAcquireLockException.class);

        assertThatExceptionOfType(CannotAcquireLockException.class)
                .isThrownBy(() -> offenderMatchService.createOrUpdateGroupedMatchesByDefendant("CASE_ID", "DEFENDANT_ID", GroupedOffenderMatchesRequest.builder().build()));

        verify(offenderMatchRepository, times(3)).findByCaseIdAndDefendantId(any(), any());
    }

    @Test
    public void givenDataIntegrityViolationExceptionExceptionThrown_whenCreateOrUpdateGroupedMatchesByDefendant_thenRetry() {
        when(offenderMatchRepository.findByCaseIdAndDefendantId(any(), any())).thenThrow(DataIntegrityViolationException.class);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> offenderMatchService.createOrUpdateGroupedMatchesByDefendant("CASE_ID", "DEFENDANT_ID", GroupedOffenderMatchesRequest.builder().build()));

        verify(offenderMatchRepository, times(3)).findByCaseIdAndDefendantId(any(), any());
    }
}
