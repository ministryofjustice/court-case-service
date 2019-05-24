package uk.gov.justice.digital.probation.court.list.courtlistservice.data.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Offence {
    private String sequence;
    private String code;
    private String title;
    private String summary;
    private String contraryToActAndSection;
    private String plea;
    private LocalDate pleaDate;
    private LocalDate convictionDate;
    private LocalDate adjournedDate;
    private String adjournedReason;
}
