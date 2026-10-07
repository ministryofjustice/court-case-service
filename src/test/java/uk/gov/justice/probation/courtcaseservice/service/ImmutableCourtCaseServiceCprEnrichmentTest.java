package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.PagedCaseListRepositoryCustom;
import uk.gov.justice.probation.courtcaseservice.service.cpr.CprEnrichmentService;
import uk.gov.justice.probation.courtcaseservice.service.flags.MultiAgencyPublicProtectionArrangementsFlagResolver;
import uk.gov.justice.probation.courtcaseservice.service.flags.SeriousFurtherOffenceFlagResolver;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImmutableCourtCaseServiceCprEnrichmentTest {

    @Mock
    private CourtRepository courtRepository;
    @Mock
    private HearingRepositoryFacade hearingRepositoryFacade;
    @Mock
    private TelemetryService telemetryService;
    @Mock
    private GroupedOffenderMatchRepository matchRepository;
    @Mock
    private DomainEventService domainEventService;
    @Mock
    private CourtCaseRepository courtCaseRepository;
    @Mock
    private ShortTermCustodyPredictorService shortTermCustodyPredictorService;
    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private PagedCaseListRepositoryCustom pagedCaseListRepositoryCustom;
    @Mock
    private SeriousFurtherOffenceFlagResolver seriousFurtherOffenceFlagResolver;
    @Mock
    private MultiAgencyPublicProtectionArrangementsFlagResolver multiAgencyPublicProtectionArrangementsFlagResolver;
    @Mock
    private CprEnrichmentService cprEnrichmentService;
    @InjectMocks
    private ImmutableCourtCaseService service;

    @Test
    void shouldSaveHearingWithoutCprEnrichmentByDefault() {
        var hearing = hearingToSave();

        var saved = service.createOrUpdateHearingByHearingId(hearing.getHearingId(), hearing).block();

        assertThat(saved).isSameAs(hearing);
        verifyNoInteractions(cprEnrichmentService);
        verify(hearingRepositoryFacade).save(hearing);
    }

    @Test
    void shouldEnrichHearingBeforeSavingWhenEnabled() {
        ReflectionTestUtils.setField(service, "enableCprHearingEnrichment", true);
        var hearing = hearingToSave();

        var saved = service.createOrUpdateHearingByHearingId(hearing.getHearingId(), hearing).block();

        assertThat(saved).isSameAs(hearing);
        var order = inOrder(cprEnrichmentService, hearingRepositoryFacade);
        order.verify(cprEnrichmentService).enrichHearingDefendants(hearing);
        order.verify(hearingRepositoryFacade).save(hearing);
    }

    private HearingEntity hearingToSave() {
        var hearing = EntityHelper.aHearingEntity(null, "1600028912");
        when(courtRepository.findByCourtCode(EntityHelper.COURT_CODE))
                .thenReturn(Optional.of(CourtEntity.builder().courtCode(EntityHelper.COURT_CODE).build()));
        when(hearingRepositoryFacade.save(hearing)).thenReturn(hearing);
        return hearing;
    }
}
