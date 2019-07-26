package uk.gov.justice.digital.probation.court.list.courtlistservice.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.justice.digital.probation.court.list.courtlistservice.data.api.CourtList;
import uk.gov.justice.digital.probation.court.list.courtlistservice.data.entity.CourtListType;
import uk.gov.justice.digital.probation.court.list.courtlistservice.transformer.SessionTransformer;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

@Service
@Slf4j
public class CourtListService {
    private final RestTemplate restTemplate;
    private final SessionTransformer sessionTransformer;

    public CourtListService(SessionTransformer sessionTransformer, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.sessionTransformer = sessionTransformer;
    }

    public CourtList courtList(String court, LocalDate date) {
        val courtList = this.restTemplate.getForObject("/court/{court}/list?date={date}", CourtListType.class, ImmutableMap.of("court", court, "date", date));
        return CourtList
                .builder()
                .courtHouse(court)
                .dateOfAppearance(date)
                .sessions(Optional.ofNullable(courtList.getSessions()).map(sessionTransformer::toSessions).orElse(Collections.emptyList()))
                .build();

    }

    public CourtList allCourtLists() {
        val courtList = this.restTemplate.getForObject("/court/list", CourtListType.class);
        return CourtList
                .builder()
                .sessions(Optional.ofNullable(courtList.getSessions()).map(sessionTransformer::toSessions).orElse(Collections.emptyList()))
                .build();
    }


}
