package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.CURRENT;

@ExtendWith(MockitoExtension.class)
public class OffenderEntityInitServiceTest {

    @Mock
    private OffenderRepository offenderRepository;

    @InjectMocks
    private OffenderEntityInitService offenderEntityInitService;

    @Test
    void givenOffenderExist_existingOffenderReturns() {
        var CRN = "CRN001";

        final var existingOffender = OffenderEntity.builder()
                .crn(CRN)
                .breach(false)
                .awaitingPsr(true)
                .probationStatus(CURRENT)
                .suspendedSentenceOrder(true)
                .preSentenceActivity(false)
                .build();

        given(offenderRepository.findByCrn(CRN)).willReturn(Optional.of(existingOffender));

        assertThat(offenderEntityInitService.findByCrn(CRN)).isEqualTo(existingOffender);
    }

    @Test
    void givenOffenderDoesNotExist_NothingIsReturned() {
        var CRN = "XYZXYZ";

        assertThat(offenderEntityInitService.findByCrn(CRN)).isEmpty();
    }
}
