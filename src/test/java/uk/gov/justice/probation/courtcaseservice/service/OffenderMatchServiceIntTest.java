package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * See ADR 0009-handling-concurrent-db-updates.md for context
 */
public class OffenderMatchServiceIntTest extends BaseIntTest {
    @Autowired
    private OffenderMatchService offenderMatchService;
    @MockitoBean
    HearingRepository hearingRepository;

    @BeforeEach
    public void setUp() {
        // Set request context to satisfy @RequestScope
        final var request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void givenCannotAcquireLockExceptionThrown__whenCreateOrUpdateGroupedMatchesByDefendant_thenRetry() {
        given(hearingRepository.findFirstByHearingDefendantsDefendantId(any())).willThrow(CannotAcquireLockException.class);

        assertThatExceptionOfType(CannotAcquireLockException.class)
                .isThrownBy(() -> offenderMatchService.createOrUpdateGroupedMatchesByDefendant("DEFENDANT_ID", GroupedOffenderMatchesRequest.builder().build()));

        verify(hearingRepository, times(3)).findFirstByHearingDefendantsDefendantId(any());
    }

    @Test
    public void givenDataIntegrityViolationExceptionExceptionThrown__whenCreateOrUpdateGroupedMatchesByDefendant_thenRetry() {
        given(hearingRepository.findFirstByHearingDefendantsDefendantId(any())).willThrow(DataIntegrityViolationException.class);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> offenderMatchService.createOrUpdateGroupedMatchesByDefendant("DEFENDANT_ID", GroupedOffenderMatchesRequest.builder().build()));

        verify(hearingRepository, times(3)).findFirstByHearingDefendantsDefendantId(any());
    }
}
