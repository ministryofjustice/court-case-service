package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantOffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;

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
    void givenFullDefendantWithOffences_whenAsEntity_thenReturn() {

        final var previouslyKnownTerminationDate = LocalDate.of(2021, Month.MARCH, 20);
        final var defendant = buildDefendant("M");

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

        assertThat(courtCaseEntity.getDefendants().get(0).getOffender().getPreviouslyKnownTerminationDate()).isEqualTo(previouslyKnownTerminationDate);
        assertThat(courtCaseEntity.getDefendants().get(0).getOffender().isPreSentenceActivity()).isEqualTo(Boolean.TRUE);
        assertThat(courtCaseEntity.getDefendants().get(0).getOffender().isBreach()).isEqualTo(Boolean.TRUE);
        assertThat(courtCaseEntity.getDefendants().get(0).getOffender().getAwaitingPsr()).isEqualTo(Boolean.TRUE);
        assertThat(courtCaseEntity.getDefendants().get(0).getOffender().getProbationStatus()).isEqualTo(ProbationStatus.CURRENT);

        assertThat(courtCaseEntity.getDefendants()).hasSize(1);
        final var defendantEntity = courtCaseEntity.getDefendants().get(0);
        assertThat(defendantEntity.getAddress()).isEqualTo(expectedAddress);
        assertThat(defendantEntity.getCrn()).isEqualTo(CRN);
        assertThat(defendantEntity.getCro()).isEqualTo(CRO);
        assertThat(defendantEntity.getCourtCase()).isSameAs(courtCaseEntity);
        assertThat(defendantEntity.getDateOfBirth()).isEqualTo(DEFENDANT_DOB);
        assertThat(defendantEntity.getDefendantName()).isEqualTo(NAME.getFullName());
        assertThat(defendantEntity.getName()).isEqualTo(NAME);
        assertThat(defendantEntity.getPnc()).isEqualTo(PNC);
        assertThat(defendantEntity.getOffender().getAwaitingPsr()).isEqualTo(Boolean.TRUE);
        assertThat(defendantEntity.getOffender().isBreach()).isEqualTo(Boolean.TRUE);
        assertThat(defendantEntity.getOffender().isPreSentenceActivity()).isEqualTo(Boolean.TRUE);
        assertThat(defendantEntity.getOffender().getPreviouslyKnownTerminationDate()).isEqualTo(previouslyKnownTerminationDate);
        assertThat(defendantEntity.getOffender().getProbationStatus()).isEqualTo(ProbationStatus.CURRENT);
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
        assertThat(offences).extracting("listNo").containsOnly(10, 20);

        final var offender = defendantEntity.getOffender();
        assertThat(offender.getCrn()).isEqualTo(CRN);
        assertThat(offender.getProbationStatus()).isSameAs(ProbationStatus.CURRENT);
        assertThat(offender.getAwaitingPsr()).isEqualTo(Boolean.TRUE);
        assertThat(offender.isBreach()).isEqualTo(Boolean.TRUE);
        assertThat(offender.isPreSentenceActivity()).isEqualTo(Boolean.TRUE);
        assertThat(offender.isSuspendedSentenceOrder()).isEqualTo(Boolean.TRUE);
        assertThat(offender.getPreviouslyKnownTerminationDate()).isEqualTo(previouslyKnownTerminationDate);
    }

    @Test
    void givenDefendantWithoutCrn_whenAsEntity_thenReturnWithNoOffender() {

        final var defendant = Defendant.builder()
            .name(NAME)
            .pnc(PNC)
            .sex("M")
            .type(DefendantType.PERSON)
            .defendantId(DEFENDANT_ID)
            .offences(List.of(OffenceRequestResponse.builder().listNo(10).build()))
            .build();
        final var request = ExtendedCourtCaseRequestResponse.builder()
            .defendants(List.of(defendant))
            .build();

        final var courtCaseEntity = request.asCourtCaseEntity();

        assertThat(courtCaseEntity.getDefendants()).hasSize(1);
        assertThat(courtCaseEntity.getDefendants().get(0).getOffences()).containsOnly(
                DefendantOffenceEntity.builder()
                        .sequence(1)
                        .listNo(10)
                        .build());
        assertThat(courtCaseEntity.getDefendants().get(0).getOffender()).isNull();
    }

    @Test
    void givenDefendantWitCrnAndWithoutNonNullableFields_whenAsEntity_thenReturnWithDefaults() {

        final var defendant = Defendant.builder()
            .name(NAME)
            .pnc(PNC)
            .sex("M")
            .type(DefendantType.PERSON)
            .defendantId(DEFENDANT_ID)
            .crn("CRN")
            .build();
        final var request = ExtendedCourtCaseRequestResponse.builder()
                .hearingDays(List.of(HearingDay.builder().sessionStartTime(LocalDateTime.now()).listNo("list1").build()))
            .defendants(List.of(defendant))
            .build();

        final var courtCaseEntity = request.asCourtCaseEntity();
        final var offender = courtCaseEntity.getDefendants().get(0).getOffender();
        assertThat(offender).isNotNull();
        assertThat(offender.isBreach()).isEqualTo(false);
        assertThat(offender.isPreSentenceActivity()).isEqualTo(false);
        assertThat(offender.isSuspendedSentenceOrder()).isEqualTo(false);
    }

    @Test
    void givenVerboseSex_whenAsEntity_thenReturn() {

        final var request = ExtendedCourtCaseRequestResponse.builder()
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
    void givenNullSex_whenAsEntity_thenReturnAsNotKnown() {

        final var request = ExtendedCourtCaseRequestResponse.builder()
            .caseNo(CASE_NO)
            .caseId(CASE_ID)
            .defendants(List.of(buildDefendant(null)))
            .build();

        final var courtCaseEntity = request.asCourtCaseEntity();

        assertThat(courtCaseEntity.getHearings()).isEmpty();
        assertThat(courtCaseEntity.getDefendants()).hasSize(1);
        assertThat(courtCaseEntity.getDefendants().get(0).getSex()).isEqualTo(Sex.NOT_KNOWN);
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
                        .probationStatus("CURRENT")
                        .sex(Sex.MALE.name())
                        .suspendedSentenceOrder(true)
                        .offences(List.of(OffenceRequestResponse.builder()
                                        .act("act")
                                        .offenceSummary("summary")
                                        .offenceTitle("title")
                                        .listNo(11)
                                        .build(),
                                OffenceRequestResponse.builder()
                                        .act("act2")
                                        .build()))
                        .build());
        assertThat(actual.getDefendants().get(1).getDefendantId()).isEqualTo("DEFENDANT_ID_2");
    }

    @Test()
    void whenListNoInNeitherOfHearingDaysAndDefendantOffences_thenThrow() {

        var hearingDays = List.of(
                HearingDay.builder().sessionStartTime(LocalDateTime.now()).build(),
                HearingDay.builder().sessionStartTime(LocalDateTime.now()).build());
        var offences = List.of(OffenceRequestResponse.builder().build());

        var courtCase = ExtendedCourtCaseRequestResponse.builder()
                .hearingDays(hearingDays).defendants(List.of(Defendant.builder().offences(offences).build(),
                        Defendant.builder().offences(offences).build())).build();
        ConflictingInputException conflictingInputException = Assertions.assertThrows(ConflictingInputException.class,
                courtCase::asCourtCaseEntity, "Exception must be thrown");
        Assertions.assertEquals(conflictingInputException.getMessage(),
                "listNo should be provided in either hearingDays[] or defendants[].offences[]");
    }

    @Test()
    void whenListNoIsInBothHearingDaysAndDefendantOffences_thenThrow() {

        var hearingDays = List.of(
                HearingDay.builder().listNo("list1").build(),
                HearingDay.builder().listNo("list2").build());
        var offences = List.of(OffenceRequestResponse.builder().listNo(20).build());

        var courtCase = ExtendedCourtCaseRequestResponse.builder()
                .hearingDays(hearingDays).defendants(List.of(Defendant.builder().offences(offences).build())).build();

        ConflictingInputException conflictingInputException = Assertions.assertThrows(ConflictingInputException.class,
                courtCase::asCourtCaseEntity, "Exception must be thrown");
        Assertions.assertEquals(conflictingInputException.getMessage(),
                "Only one of hearingDays[].listNo and defendants[].offences[].listNo must be provided");
    }

    @Test()
    void whenListNoIsNotProvidedInAllOfTheHearingDays_thenThrow() {

        var hearingDays = List.of(
                HearingDay.builder().listNo("list1").build(),
                HearingDay.builder().build(),
                HearingDay.builder().listNo("list2").build());
        var offences = List.of(OffenceRequestResponse.builder().build());

        var courtCase = ExtendedCourtCaseRequestResponse.builder()
                .hearingDays(hearingDays).defendants(List.of(Defendant.builder().offences(offences).build())).build();

        ConflictingInputException conflictingInputException = Assertions.assertThrows(ConflictingInputException.class,
                courtCase::asCourtCaseEntity, "Exception must be thrown");
        Assertions.assertEquals(conflictingInputException.getMessage(),
                "listNo is missing from one or more hearingDays[]");
    }

    @Test()
    void whenListNoIsNotProvidedInAllOfTheOffences_thenThrow() {

        var hearingDays = List.of(
                HearingDay.builder().build(),
                HearingDay.builder().build());
        var offences1 = List.of(
                    OffenceRequestResponse.builder().listNo(10).build(),
                    OffenceRequestResponse.builder().listNo(10).build()
                );
         var offences2 = List.of(
                    OffenceRequestResponse.builder().listNo(30).build(),
                    OffenceRequestResponse.builder().build()
                );

        var courtCase = ExtendedCourtCaseRequestResponse.builder()
                .hearingDays(hearingDays).defendants(
                        List.of(Defendant.builder().offences(offences1).build(),
                        Defendant.builder().offences(offences2).build())
                ).build();

        ConflictingInputException conflictingInputException = Assertions.assertThrows(ConflictingInputException.class,
                courtCase::asCourtCaseEntity, "Exception must be thrown");
        Assertions.assertEquals(conflictingInputException.getMessage(),
                "listNo missing in one or more defendants[].offences[]");
    }

    @Test()
    void givenPutWith_NoListNoInHearingDays_WithListNoInDefendantOffences_Accepted() {

        var hearingDays = List.of(
                HearingDay.builder().sessionStartTime(LocalDateTime.now()).build(),
                HearingDay.builder().sessionStartTime(LocalDateTime.now()).build());
        var offences1 = List.of(
                    OffenceRequestResponse.builder().listNo(10).build(),
                    OffenceRequestResponse.builder().listNo(20).build()
                );
         var offences2 = List.of(
                    OffenceRequestResponse.builder().listNo(30).build()
                );

        var courtCase = ExtendedCourtCaseRequestResponse.builder()
                .hearingDays(hearingDays).defendants(
                        List.of(Defendant.builder().name(NamePropertiesEntity.builder().forename1("Foreone").surname("Surone").build()).offences(offences1).build(),
                        Defendant.builder().name(NamePropertiesEntity.builder().forename1("Foretwo").surname("Surtwo").build()).offences(offences2).build())
                ).build();

        var courtCaseEntity = courtCase.asCourtCaseEntity();
        assertThat(courtCaseEntity.getDefendants()).hasSize(2);
        assertThat(courtCaseEntity.getDefendants().get(0).getOffences()).extracting("listNo").containsOnly(10, 20);
        assertThat(courtCaseEntity.getDefendants().get(1).getOffences()).extracting("listNo").containsOnly(30);
    }

    @Test()
    void givenPutWith_NoListNoInDefendantOffences_WithListNoInHearingDays_Accepted() {

        var hearingDays = List.of(
                HearingDay.builder().listNo("10").sessionStartTime(LocalDateTime.now()).build(),
                HearingDay.builder().listNo("20").sessionStartTime(LocalDateTime.now()).build());
        var offences1 = List.of(
                    OffenceRequestResponse.builder().build(),
                    OffenceRequestResponse.builder().build()
                );
         var offences2 = List.of(
                    OffenceRequestResponse.builder().build()
                );

        var courtCase = ExtendedCourtCaseRequestResponse.builder()
                .hearingDays(hearingDays).defendants(
                        List.of(Defendant.builder().name(NamePropertiesEntity.builder().forename1("Foreone").surname("Surone").build()).offences(offences1).build(),
                        Defendant.builder().name(NamePropertiesEntity.builder().forename1("Foretwo").surname("Surtwo").build()).offences(offences2).build())
                ).build();

        var courtCaseEntity = courtCase.asCourtCaseEntity();
        assertThat(courtCaseEntity.getHearings()).extracting("listNo").containsOnly("10", "20");
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
                                .offender(OffenderEntity.builder()
                                    .crn("crn")
                                    .preSentenceActivity(true)
                                    .previouslyKnownTerminationDate(LocalDate.of(2001, 1, 1))
                                    .suspendedSentenceOrder(true)
                                    .awaitingPsr(true)
                                    .breach(true)
                                    .probationStatus(ProbationStatus.CURRENT)
                                    .build())
                                .pnc("pnc")
                                .cro("cro")
                                .dateOfBirth(LocalDate.of(2000, 1, 1))
                                .name(NamePropertiesEntity.builder()
                                        .forename1("forename1")
                                        .forename2("forename2")
                                        .surname("surname")
                                        .build())
                                .defendantId("defendantId")
                                .sex(Sex.MALE)
                                .offences(List.of(DefendantOffenceEntity.builder()
                                                .act("act")
                                                .summary("summary")
                                                .title("title")
                                                .sequence(1)
                                                .listNo(11)
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
                    .listNo(10)
                    .build(),
                OffenceRequestResponse.builder()
                    .offenceTitle("TITLE2")
                    .offenceSummary("SUMMARY2")
                    .act("ACT2")
                    .listNo(20)
                    .build()))
            .build();
    }
}
