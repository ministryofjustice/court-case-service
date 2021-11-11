package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantOffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.controller.model.ExtendedCourtCaseRequestResponse.DEFAULT_SOURCE;
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

class ExtendedCourtCaseRequestResponseTest {

    @Test
    void givenHearings_whenAsEntity_thenReturn() {

        final var request = ExtendedCourtCaseRequestResponse.builder()
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

        final var defendant = buildDefendant();

        final var request = ExtendedCourtCaseRequestResponse.builder()
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
        assertThat(courtCaseEntity.getDefendantSex().getName()).isEqualTo("M");
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
        assertThat(defendantEntity.getSex()).isSameAs(Sex.MALE);
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

        final var request = ExtendedCourtCaseRequestResponse.builder()
                .caseNo(CASE_NO)
                .caseId(CASE_ID)
                .build();

        final var courtCaseEntity = request.asCourtCaseEntity();

        assertThat(courtCaseEntity.getHearings()).isEmpty();
        assertThat(courtCaseEntity.getDefendants()).isEmpty();
        assertThat(courtCaseEntity.getCaseNo()).isEqualTo(CASE_NO);
        assertThat(courtCaseEntity.getSourceType()).isEqualTo(DEFAULT_SOURCE);
    }

    @Test
    void givenVerboseSex_whenAsEntity_thenReturn() {

        final var request = ExtendedCourtCaseRequestResponse.builder()
            .courtCode(COURT_CODE)
            .caseNo(CASE_NO)
            .caseId(CASE_ID)
            .defendants(List.of(buildDefendant("male")))
            .build();

        final var courtCaseEntity = request.asCourtCaseEntity();

        assertThat(courtCaseEntity.getHearings()).isEmpty();
        assertThat(courtCaseEntity.getDefendants()).hasSize(1);
        assertThat(courtCaseEntity.getDefendants().get(0).getSex()).isEqualTo(Sex.MALE);
    }

    @Test
    void whenOfCourtCase_thenReturn() {

        final var courtCaseEntity = buildEntity();

        final var actual = ExtendedCourtCaseRequestResponse.of(courtCaseEntity);

        assertThat(actual.getSource()).isEqualTo("LIBRA");
        assertThat(actual.getCaseId()).isEqualTo(CASE_ID);
        assertThat(actual.getCaseNo()).isEqualTo(CASE_NO);
        assertThat(actual.getHearingDays().get(0)).isEqualTo(HearingDay.builder()
                .courtRoom(COURT_ROOM)
                        .courtCode(COURT_CODE)
                .sessionStartTime(LocalDateTime.of(2021, 10, 5, 15, 15, 15))
                .listNo("1")
                .build());
        assertThat(actual.getHearingDays().get(1).getListNo()).isEqualTo("2");
        assertThat(actual.getDefendants().get(0)).isEqualTo(Defendant.builder()
                        .address(AddressRequestResponse.builder()
                                .line1("line1")
                                .line2("line2")
                                .line3("line3")
                                .line4("line4")
                                .line5("line5")
                                .postcode("postcode")
                                .build())
                        .awaitingPsr(true)
                        .breach(true)
                        .crn("crn")
                        .pnc("pnc")
                        .cro("cro")
                        .dateOfBirth(LocalDate.of(2000, 1, 1))
                        .name(NamePropertiesEntity.builder()
                                .forename1("forename1")
                                .forename2("forename2")
                                .surname("surname")
                                .build())
                        .defendantId("defendantId")
                        .preSentenceActivity(true)
                        .previouslyKnownTerminationDate(LocalDate.of(2001, 1, 1))
                        .probationStatus("ProbationStatus")
                        .sex(Sex.MALE.name())
                        .suspendedSentenceOrder(true)
                        .offences(List.of(OffenceRequestResponse.builder()
                                        .act("act")
                                        .offenceSummary("summary")
                                        .offenceTitle("title")
                                        .build(),
                                OffenceRequestResponse.builder()
                                        .act("act2")
                                        .build()))
                        .build());
        assertThat(actual.getDefendants().get(1).getDefendantId()).isEqualTo("DEFENDANT_ID_2");
    }

    private CourtCaseEntity buildEntity() {
        return CourtCaseEntity.builder()
                .sourceType(SourceType.LIBRA)
                .caseId(CASE_ID)
                .caseNo(CASE_NO)
                .hearings(List.of(HearingEntity.builder()
                                .courtCode(COURT_CODE)
                                .courtRoom(COURT_ROOM)
                                .hearingDay(LocalDate.of(2021, 10, 5))
                                .hearingTime(LocalTime.of(15, 15, 15))
                                .listNo("1")
                                .build(),
                        HearingEntity.builder()
                                .courtCode(COURT_CODE)
                                .listNo("2")
                                .build()
                ))
                .defendants(List.of(DefendantEntity.builder()
                                .address(AddressPropertiesEntity.builder()
                                        .line1("line1")
                                        .line2("line2")
                                        .line3("line3")
                                        .line4("line4")
                                        .line5("line5")
                                        .postcode("postcode")
                                        .build())
                                .awaitingPsr(true)
                                .breach(true)
                                .crn("crn")
                                .pnc("pnc")
                                .cro("cro")
                                .dateOfBirth(LocalDate.of(2000, 1, 1))
                                .name(NamePropertiesEntity.builder()
                                        .forename1("forename1")
                                        .forename2("forename2")
                                        .surname("surname")
                                        .build())
                                .defendantId("defendantId")
                                .preSentenceActivity(true)
                                .previouslyKnownTerminationDate(LocalDate.of(2001, 1, 1))
                                .probationStatus("ProbationStatus")
                                .sex(Sex.MALE)
                                .suspendedSentenceOrder(true)
                                .offences(List.of(DefendantOffenceEntity.builder()
                                                .act("act")
                                                .summary("summary")
                                                .title("title")
                                                .sequence(1)
                                                .build(),
                                        DefendantOffenceEntity.builder()
                                                .act("act2")
                                                .build()
                                ))
                                .build(),
                        DefendantEntity.builder()
                                .defendantId("DEFENDANT_ID_2")
                                .build()))
                .build();
    }

    private Defendant buildDefendant(String sex) {
        return Defendant.builder()
            .awaitingPsr(Boolean.TRUE)
            .address(AddressRequestResponse.builder()
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
            .sex(Sex.fromString(sex).name())
            .suspendedSentenceOrder(Boolean.TRUE)
            .type(DefendantType.PERSON)
            .defendantId(DEFENDANT_ID)
            .offences(List.of(OffenceRequestResponse.builder()
                    .offenceTitle("TITLE1")
                    .offenceSummary("SUMMARY1")
                    .act("ACT1")
                    .build(),
                OffenceRequestResponse.builder()
                    .offenceTitle("TITLE2")
                    .offenceSummary("SUMMARY2")
                    .act("ACT2")
                    .build()))
            .build();
    }

    private Defendant buildDefendant() {
        return buildDefendant("M");
    }
}
