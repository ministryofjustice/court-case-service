package uk.gov.justice.probation.courtcaseservice.service.model;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class Conviction {
    private String convictionId;
    private Boolean active;
    private List<Offence> offences;
    private Sentence sentence;
    private LocalDate convictionDate;
    private LocalDate endDate;
}
