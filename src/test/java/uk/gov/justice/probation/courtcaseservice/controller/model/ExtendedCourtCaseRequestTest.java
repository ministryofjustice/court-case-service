package uk.gov.justice.probation.courtcaseservice.controller.model;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_NO;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_ROOM;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRO;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_DOB;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.LIST_NO;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.NAME;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.PNC;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.SESSION_START_TIME;

class ExtendedCourtCaseRequestTest {

    @Test
    void givenHearings_whenAsEntity_thenReturn() {

        final var request = ExtendedCourtCaseRequest.builder()
                .hearingDays(List.of(
                    HearingDay.builder()
                        .listNo(LIST_NO)
                        .sessionStartTime(SESSION_START_TIME)
                        .courtRoom(COURT_ROOM)
                        .courtCode(COURT_CODE)
                    .build(),
                    HearingDay.builder()
                        .listNo("2nd")
                        .sessionStartTime(SESSION_START_TIME.plusMinutes(30))
                        .courtRoom(COURT_ROOM)
                        .courtCode(COURT_CODE)
                    .build()))
                .build();

        final var courtCaseEntity = request.asCourtCaseEntity();

        // top level field checks (hearing based) to be retired
        assertThat(courtCaseEntity.getCourtRoom()).isEqualTo(COURT_ROOM);
        assertThat(courtCaseEntity.getSessionStartTime()).isEqualTo(SESSION_START_TIME);
        assertThat(courtCaseEntity.getListNo()).isEqualTo(LIST_NO);

        assertThat(courtCaseEntity.getHearings()).hasSize(2);
        assertThat(courtCaseEntity.getHearings()).extracting("listNo").contains("1st", "2nd");
        assertThat(courtCaseEntity.getHearings()).extracting("courtCode").containsOnly(COURT_CODE);
        assertThat(courtCaseEntity.getHearings()).extracting("courtRoom").containsOnly(COURT_ROOM);
        assertThat(courtCaseEntity.getHearings()).extracting("courtRoom").containsOnly(COURT_ROOM);
        assertThat(courtCaseEntity.getHearings()).extracting("hearingDay").containsOnly(SESSION_START_TIME.toLocalDate());
        final var localTime = SESSION_START_TIME.toLocalTime();
        assertThat(courtCaseEntity.getHearings()).extracting("hearingTime").containsOnly(localTime, localTime.plusMinutes(30));
        assertThat(courtCaseEntity.getHearings().get(0).getCourtCase()).isSameAs(courtCaseEntity);
    }

    @Test
    void givenDefendantsWithOffences_whenAsEntity_thenReturn() {

        final var defendant = Defendant.builder()
            .awaitingPsr(Boolean.TRUE)
            .address(AddressRequest.builder()
                .line1("PIC TEAM")
                .line2("Digital Studio")
                .line3("32 Scotland Street")
                .line4("Sheffield")
                .line5("South Yorkshire")
                .postcode("S3 7DQ")
                .build())
            .breach(Boolean.TRUE)
            .crn(CRN)
            .cro(CRO)
            .dateOfBirth(DEFENDANT_DOB)
            .name(NAME)
            .pnc(PNC)
            .preSentenceActivity(Boolean.TRUE)
            .previouslyKnownTerminationDate(LocalDate.of(2021, Month.MARCH, 20))
            .probationStatus("CURRENT")
            .sex("M")
            .suspendedSentenceOrder(Boolean.TRUE)
            .type(DefendantType.PERSON)
            .defendantId(DEFENDANT_ID)
            .offences(List.of(OffenceRequest.builder()
                                .offenceTitle("TITLE1")
                                .offenceSummary("SUMMARY1")
                                .act("ACT1")
                                .build(),
                            OffenceRequest.builder()
                                .offenceTitle("TITLE2")
                                .offenceSummary("SUMMARY2")
                                .act("ACT2")
                                .build()))
            .build();

        final var request = ExtendedCourtCaseRequest.builder()
            .defendants(List.of(defendant))
            .build();

        final var courtCaseEntity = request.asCourtCaseEntity();

        final var expectedAddress = AddressPropertiesEntity.builder()
            .line1("PIC TEAM")
            .line2("Digital Studio")
            .line3("32 Scotland Street")
            .line4("Sheffield")
            .line5("South Yorkshire")
            .postcode("S3 7DQ")
            .build();

        // top level field checks (defendant based) to be retired
        assertThat(courtCaseEntity.getName()).isEqualTo(NAME);
        assertThat(courtCaseEntity.getDefendantName()).isEqualTo(NAME.getFullName());
        assertThat(courtCaseEntity.getDefendantType()).isSameAs(DefendantType.PERSON);
        assertThat(courtCaseEntity.getDefendantAddress()).isEqualTo(expectedAddress);
        assertThat(courtCaseEntity.getCrn()).isEqualTo(CRN);
        assertThat(courtCaseEntity.getPnc()).isEqualTo(PNC);
        assertThat(courtCaseEntity.getCro()).isEqualTo(CRO);
        assertThat(courtCaseEntity.getDefendantDob()).isEqualTo(DEFENDANT_DOB);
        assertThat(courtCaseEntity.getDefendantSex()).isEqualTo("M");
        assertThat(courtCaseEntity.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2021, Month.MARCH, 20));
        assertThat(courtCaseEntity.getPreSentenceActivity()).isEqualTo(Boolean.TRUE);
        assertThat(courtCaseEntity.getBreach()).isEqualTo(Boolean.TRUE);
        assertThat(courtCaseEntity.getAwaitingPsr()).isEqualTo(Boolean.TRUE);
        assertThat(courtCaseEntity.getProbationStatus()).isEqualTo("CURRENT");
        assertThat(courtCaseEntity.getOffences()).hasSize(2);

        assertThat(courtCaseEntity.getDefendants()).hasSize(1);
        final var defendantEntity = courtCaseEntity.getDefendants().get(0);
        assertThat(defendantEntity.getAwaitingPsr()).isEqualTo(Boolean.TRUE);
        assertThat(defendantEntity.getAddress()).isEqualTo(expectedAddress);
        assertThat(defendantEntity.getBreach()).isEqualTo(Boolean.TRUE);
        assertThat(defendantEntity.getCrn()).isEqualTo(CRN);
        assertThat(defendantEntity.getCro()).isEqualTo(CRO);
        assertThat(defendantEntity.getCourtCase()).isSameAs(courtCaseEntity);
        assertThat(defendantEntity.getDateOfBirth()).isEqualTo(DEFENDANT_DOB);
        assertThat(defendantEntity.getDefendantName()).isEqualTo(NAME.getFullName());
        assertThat(defendantEntity.getName()).isEqualTo(NAME);
        assertThat(defendantEntity.getPnc()).isEqualTo(PNC);
        assertThat(defendantEntity.getPreSentenceActivity()).isEqualTo(Boolean.TRUE);
        assertThat(defendantEntity.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2021, Month.MARCH, 20));
        assertThat(defendantEntity.getProbationStatus()).isEqualTo("CURRENT");
        assertThat(defendantEntity.getSex()).isEqualTo("M");
        assertThat(defendantEntity.getDefendantId()).isEqualTo(DEFENDANT_ID);

        final var offences = defendantEntity.getOffences();
        assertThat(offences).hasSize(2);
        assertThat(offences.get(0).getDefendant()).isSameAs(defendantEntity);
        assertThat(offences.get(1).getDefendant()).isSameAs(defendantEntity);
        assertThat(offences).extracting("act").containsOnly("ACT1", "ACT2");
        assertThat(offences).extracting("sequence").containsOnly(1, 2);
        assertThat(offences).extracting("summary").containsOnly("SUMMARY1", "SUMMARY2");
        assertThat(offences).extracting("title").containsOnly("TITLE1", "TITLE2");
    }

    @Test
    void givenNullHearingsAndDefendants_whenAsEntity_thenReturn() {

        final var request = ExtendedCourtCaseRequest.builder()
            .courtCode(COURT_CODE)
            .caseNo(CASE_NO)
            .caseId(CASE_ID)
            .build();

        final var courtCaseEntity = request.asCourtCaseEntity();

        assertThat(courtCaseEntity.getHearings()).hasSize(0);
        assertThat(courtCaseEntity.getDefendants()).hasSize(0);
        assertThat(courtCaseEntity.getCaseNo()).isEqualTo(CASE_NO);
        assertThat(courtCaseEntity.getCourtCode()).isEqualTo(COURT_CODE);
    }

}
