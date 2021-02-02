package uk.gov.justice.probation.courtcaseservice.controller;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtListResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourtControllerTest {

    public static final CourtEntity LEICESTER = CourtEntity.builder().courtCode("B33HU").name("Leicester").build();

    @Mock
    private CourtService courtService;

    @InjectMocks
    private CourtController courtController;

    @Test
    public void getCourts_shouldReturnCourtsSorted() {
        when(courtService.getCourts()).thenReturn(List.of(LEICESTER));

        CourtListResponse response = courtController.getCourts();

        assertThat(response.getCourts()).hasSize(1);
        assertThat(response.getCourts()).extracting("name").containsExactly("Leicester");
    }

}
