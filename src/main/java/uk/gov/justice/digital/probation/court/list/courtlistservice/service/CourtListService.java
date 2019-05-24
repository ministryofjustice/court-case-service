package uk.gov.justice.digital.probation.court.list.courtlistservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.probation.court.list.courtlistservice.data.api.CourtList;

import java.time.LocalDate;

@Service
@Slf4j
public class CourtListService {
    public CourtList courtList(String court, LocalDate date) {
        return CourtList
                .builder()
                .courtName(court)
                .build();

    }

    public CourtList allCourtLists() {
        return CourtList
                .builder()
                .courtName("Sheffield")
                .build();
    }


}
