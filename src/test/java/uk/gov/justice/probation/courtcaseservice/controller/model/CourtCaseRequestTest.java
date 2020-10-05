package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class CourtCaseRequestTest {

    private static final LocalDateTime SESSION_START_TIME = LocalDateTime.of(2020, 10, 5, 9, 30);
    private static final LocalDate PREVIOUS_TERMINATION_DATE = LocalDate.of(2020, 9, 5);
    private static final LocalDate DOB = LocalDate.of(1985, 8, 1);
    private static final boolean SUSPENDED_SENTENCE_ORDER = true;
    private static final boolean BREACH = false;

    @Test
    void asEntity() {
        final var request = CourtCaseRequest.builder()
        .caseId("CASE_ID")
        .caseNo("CASE_NO")
        .courtCode("COURT_CODE")
        .courtRoom("COURT_ROOM")
        .sessionStartTime(SESSION_START_TIME)
        .probationStatus("PROBATION_STATUS")
        .previouslyKnownTerminationDate(PREVIOUS_TERMINATION_DATE)
        .suspendedSentenceOrder(SUSPENDED_SENTENCE_ORDER)
        .breach(BREACH)
        .offences(Arrays.asList(
                new OffenceRequest("OFFENCE_TITLE1", "OFFENCE_SUMMARY1","ACT1"),
                new OffenceRequest("OFFENCE_TITLE2", "OFFENCE_SUMMARY2","ACT2")
            )
        )
        .defendantName("DEFENDANT_NAME")
        .defendantAddress(new AddressRequest("LINE1", "LINE2", "LINE3", "LINE4", "LINE5", "POSTCODE"))
        .defendantDob(DOB)
        .defendantSex("SEX")
        .crn("CRN")
        .pnc("PNC")
        .cro("CRO")
        .listNo("LIST_NO")
        .nationality1("NATIONALITY_1")
        .nationality2("NATIONALITY_2")
        .build();

        final var entity = request.asEntity();

        assertThat(entity.getCaseId()).isEqualTo("CASE_ID");
        assertThat(entity.getCaseNo()).isEqualTo("CASE_NO");
        assertThat(entity.getCourtCode()).isEqualTo("COURT_CODE");
        assertThat(entity.getCourtRoom()).isEqualTo("COURT_ROOM");
        assertThat(entity.getSessionStartTime()).isEqualTo(SESSION_START_TIME);
        assertThat(entity.getProbationStatus()).isEqualTo("PROBATION_STATUS");
        assertThat(entity.getPreviouslyKnownTerminationDate()).isEqualTo(PREVIOUS_TERMINATION_DATE);
        assertThat(entity.getSuspendedSentenceOrder()).isEqualTo(SUSPENDED_SENTENCE_ORDER);
        assertThat(entity.getBreach()).isEqualTo(BREACH);
        assertThat(entity.getDefendantName()).isEqualTo("DEFENDANT_NAME");
        assertThat(entity.getDefendantAddress().getLine1()).isEqualTo("LINE1");
        assertThat(entity.getDefendantAddress().getLine2()).isEqualTo("LINE2");
        assertThat(entity.getDefendantAddress().getLine3()).isEqualTo("LINE3");
        assertThat(entity.getDefendantAddress().getLine4()).isEqualTo("LINE4");
        assertThat(entity.getDefendantAddress().getLine5()).isEqualTo("LINE5");
        assertThat(entity.getDefendantAddress().getPostcode()).isEqualTo("POSTCODE");
        assertThat(entity.getDefendantDob()).isEqualTo(DOB);
        assertThat(entity.getDefendantSex()).isEqualTo("SEX");
        assertThat(entity.getCrn()).isEqualTo("CRN");
        assertThat(entity.getPnc()).isEqualTo("PNC");
        assertThat(entity.getCro()).isEqualTo("CRO");
        assertThat(entity.getListNo()).isEqualTo("LIST_NO");
        assertThat(entity.getNationality1()).isEqualTo("NATIONALITY_1");
        assertThat(entity.getNationality2()).isEqualTo("NATIONALITY_2");
        assertThat(entity.getOffences().get(0).getSequenceNumber()).isEqualTo(1);
        assertThat(entity.getOffences().get(0).getOffenceTitle()).isEqualTo("OFFENCE_TITLE1");
        assertThat(entity.getOffences().get(0).getOffenceSummary()).isEqualTo("OFFENCE_SUMMARY1");
        assertThat(entity.getOffences().get(0).getAct()).isEqualTo("ACT1");
        assertThat(entity.getOffences().get(1).getSequenceNumber()).isEqualTo(2);
        assertThat(entity.getOffences().get(1).getOffenceTitle()).isEqualTo("OFFENCE_TITLE2");
        assertThat(entity.getOffences().get(1).getOffenceSummary()).isEqualTo("OFFENCE_SUMMARY2");
        assertThat(entity.getOffences().get(1).getAct()).isEqualTo("ACT2");
    }
}