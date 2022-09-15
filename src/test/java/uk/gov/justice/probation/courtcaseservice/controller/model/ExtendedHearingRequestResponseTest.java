package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEventType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.controller.model.ExtendedHearingRequestResponse.DEFAULT_SOURCE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_NO;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_ROOM;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRO;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_DOB;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_PHONE_NUMBER;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_PHONE_NUMBER_ENTITY;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.LIST_NO;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.NAME;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.OFFENDER_PNC;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.PNC;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.SESSION_START_TIME;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.URN;

class ExtendedHearingRequestResponseTest {

    @Test
    void givenHearings_whenAsEntity_thenReturn() {

        final var request = ExtendedHearingRequestResponse.builder()
                .caseId("CASE_ID")
                .hearingId("HEARING_ID")
                .hearingEventType("ConfirmedOrUpdated")
                .hearingType("sentence")
                .urn(URN)
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

        final var hearingEntity = request.asHearingEntity();

        assertThat(hearingEntity.getCaseId()).isEqualTo("CASE_ID");
        assertThat(hearingEntity.getHearingId()).isEqualTo("HEARING_ID");
        assertThat(hearingEntity.getListNo()).isEqualTo(LIST_NO);
        assertThat(hearingEntity.getCourtCase().getUrn()).isEqualTo(URN);
        assertThat(hearingEntity.getHearingType()).isEqualTo("sentence");
        assertThat(hearingEntity.getHearingEventType()).isEqualTo(HearingEventType.CONFIRMED_OR_UPDATED);
        assertThat(hearingEntity.getHearingDays()).hasSize(2);
        assertThat(hearingEntity.getHearingDays()).extracting("listNo").contains("1st", "2nd");
        assertThat(hearingEntity.getHearingDays()).extracting("courtCode").containsOnly(COURT_CODE);
        assertThat(hearingEntity.getHearingDays()).extracting("courtRoom").containsOnly(COURT_ROOM);
        assertThat(hearingEntity.getHearingDays()).extracting("courtRoom").containsOnly(COURT_ROOM);
        assertThat(hearingEntity.getHearingDays()).extracting("day").containsOnly(SESSION_START_TIME.toLocalDate());
        final var localTime = SESSION_START_TIME.toLocalTime();
        assertThat(hearingEntity.getHearingDays()).extracting("time").containsOnly(localTime, localTime.plusMinutes(30));
        assertThat(hearingEntity.getHearingDays().get(0).getHearing()).isSameAs(hearingEntity);
    }

    @Test
    void givenNoCaseId_whenAsEntity_thenReturnCaseIdAsHearingId() {

        final var request = ExtendedHearingRequestResponse.builder()
                .caseId("CASE_ID")
                .urn(URN)
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

        final var hearingEntity = request.asHearingEntity();

        assertThat(hearingEntity.getCaseId()).isEqualTo("CASE_ID");
        assertThat(hearingEntity.getHearingId()).isEqualTo("CASE_ID");
        assertThat(hearingEntity.getCourtCase().getUrn()).isEqualTo(URN);

        assertThat(hearingEntity.getHearingDays()).hasSize(2);
        assertThat(hearingEntity.getHearingDays()).extracting("listNo").contains("1st", "2nd");
        assertThat(hearingEntity.getHearingDays()).extracting("courtCode").containsOnly(COURT_CODE);
        assertThat(hearingEntity.getHearingDays()).extracting("courtRoom").containsOnly(COURT_ROOM);
        assertThat(hearingEntity.getHearingDays()).extracting("courtRoom").containsOnly(COURT_ROOM);
        assertThat(hearingEntity.getHearingDays()).extracting("day").containsOnly(SESSION_START_TIME.toLocalDate());
        final var localTime = SESSION_START_TIME.toLocalTime();
        assertThat(hearingEntity.getHearingDays()).extracting("time").containsOnly(localTime, localTime.plusMinutes(30));
        assertThat(hearingEntity.getHearingDays().get(0).getHearing()).isSameAs(hearingEntity);
    }

    @Test
    void givenFullDefendantWithOffences_whenAsEntity_thenReturn() {

        final var previouslyKnownTerminationDate = LocalDate.of(2021, Month.MARCH, 20);
        final var defendant = buildDefendant("M");

        final var request = ExtendedHearingRequestResponse.builder()
                .defendants(List.of(defendant))
                .build();

        final var hearingEntity = request.asHearingEntity();

        final var expectedAddress = AddressPropertiesEntity.builder()
                .line1("PIC TEAM")
                .line2("Digital Studio")
                .line3("32 Scotland Street")
                .line4("Sheffield")
                .line5("South Yorkshire")
                .postcode("S3 7DQ")
                .build();

        assertThat(hearingEntity.getHearingDefendants()).hasSize(1);

        final var hearingDefendantEntity = hearingEntity.getHearingDefendants().get(0);
        assertThat(hearingDefendantEntity.getHearing()).isSameAs(hearingEntity);
        assertThat(hearingDefendantEntity.getDefendantId()).isSameAs(DEFENDANT_ID);

        final var defendantEntity = hearingDefendantEntity.getDefendant();
        assertThat(defendantEntity.getDefendantId()).isEqualTo(DEFENDANT_ID);
        assertThat(defendantEntity.getAddress()).isEqualTo(expectedAddress);
        assertThat(defendantEntity.getCrn()).isEqualTo(CRN);
        assertThat(defendantEntity.getCro()).isEqualTo(CRO);
        assertThat(defendantEntity.getDateOfBirth()).isEqualTo(DEFENDANT_DOB);
        assertThat(defendantEntity.getDefendantName()).isEqualTo(NAME.getFullName());
        assertThat(defendantEntity.getName()).isEqualTo(NAME);
        assertThat(defendantEntity.getPnc()).isEqualTo(PNC);
        assertThat(defendantEntity.getSex()).isSameAs(Sex.MALE);
        assertThat(defendantEntity.getPhoneNumber()).isEqualTo(DEFENDANT_PHONE_NUMBER_ENTITY);

        final var offenderEntity = defendantEntity.getOffender();
        assertThat(offenderEntity.getPreviouslyKnownTerminationDate()).isEqualTo(previouslyKnownTerminationDate);
        assertThat(offenderEntity.isPreSentenceActivity()).isEqualTo(Boolean.TRUE);
        assertThat(offenderEntity.isBreach()).isEqualTo(Boolean.TRUE);
        assertThat(offenderEntity.getAwaitingPsr()).isEqualTo(Boolean.TRUE);
        assertThat(offenderEntity.getProbationStatus()).isEqualTo(OffenderProbationStatus.CURRENT);
        assertThat(offenderEntity.isSuspendedSentenceOrder()).isEqualTo(Boolean.TRUE);
        assertThat(offenderEntity.getCrn()).isEqualTo(CRN);
        assertThat(offenderEntity.getPnc()).isEqualTo(OFFENDER_PNC);

        final var offences = hearingDefendantEntity.getOffences();
        assertThat(offences).hasSize(2);
        assertThat(offences.get(0).getHearingDefendant().getDefendant()).isSameAs(defendantEntity);
        assertThat(offences.get(1).getHearingDefendant().getDefendant()).isSameAs(defendantEntity);
        assertThat(offences).extracting("act").containsOnly("ACT1", "ACT2");
        assertThat(offences).extracting("sequence").containsOnly(1, 2);
        assertThat(offences).extracting("summary").containsOnly("SUMMARY1", "SUMMARY2");
        assertThat(offences).extracting("title").containsOnly("TITLE1", "TITLE2");
        assertThat(offences).extracting("listNo").containsOnly(10, 20);
    }

    @Test
    void givenDefendantWithoutCrn_whenAsEntity_thenReturnWithNoOffender() {

        final var defendant = Defendant.builder()
                .name(NAME)
                .pnc(PNC)
                .sex("M")
                .type(DefendantType.PERSON)
                .defendantId(DEFENDANT_ID)
                .build();
        final var request = ExtendedHearingRequestResponse.builder()
                .defendants(List.of(defendant))
                .build();

        final var hearingEntity = request.asHearingEntity();

        assertThat(hearingEntity.getHearingDefendants()).hasSize(1);
        assertThat(hearingEntity.getHearingDefendants().get(0).getOffences()).isEmpty();
        assertThat(hearingEntity.getHearingDefendants().get(0).getDefendant().getOffender()).isNull();
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
        final var request = ExtendedHearingRequestResponse.builder()
                .defendants(List.of(defendant))
                .build();

        final var hearingEntity = request.asHearingEntity();
        final var offender = hearingEntity.getHearingDefendants().get(0).getDefendant().getOffender();
        assertThat(offender).isNotNull();
        assertThat(offender.isBreach()).isEqualTo(false);
        assertThat(offender.isPreSentenceActivity()).isEqualTo(false);
        assertThat(offender.isSuspendedSentenceOrder()).isEqualTo(false);
    }

    @Test
    void givenNullHearingsAndDefendants_whenAsEntity_thenReturn() {

        final var request = ExtendedHearingRequestResponse.builder()
                .caseNo(CASE_NO)
                .caseId(CASE_ID)
                .build();

        final var hearingEntity = request.asHearingEntity();

        assertThat(hearingEntity.getHearingDays()).isEmpty();
        assertThat(hearingEntity.getHearingDefendants()).isEmpty();
        assertThat(hearingEntity.getCaseNo()).isEqualTo(CASE_NO);
        assertThat(hearingEntity.getSourceType()).isEqualTo(DEFAULT_SOURCE);
    }

    @Test
    void givenVerboseSex_whenAsEntity_thenReturn() {

        final var request = ExtendedHearingRequestResponse.builder()
                .caseNo(CASE_NO)
                .caseId(CASE_ID)
                .defendants(List.of(buildDefendant("male")))
                .build();

        final var hearingEntity = request.asHearingEntity();

        assertThat(hearingEntity.getHearingDays()).isEmpty();
        assertThat(hearingEntity.getHearingDefendants()).hasSize(1);
        assertThat(hearingEntity.getHearingDefendants().get(0).getDefendant().getSex()).isEqualTo(Sex.MALE);
    }

    @Test
    void givenNullSex_whenAsEntity_thenReturnAsNotKnown() {

        final var request = ExtendedHearingRequestResponse.builder()
                .caseNo(CASE_NO)
                .caseId(CASE_ID)
                .defendants(List.of(buildDefendant(null)))
                .build();

        final var hearingEntity = request.asHearingEntity();

        assertThat(hearingEntity.getHearingDays()).isEmpty();
        assertThat(hearingEntity.getHearingDefendants()).hasSize(1);
        assertThat(hearingEntity.getHearingDefendants().get(0).getDefendant().getSex()).isEqualTo(Sex.NOT_KNOWN);
    }

    @Test
    void whenOfCourtCase_thenReturn() {

        final var hearingEntity = buildEntity();

        final var actual = ExtendedHearingRequestResponse.of(hearingEntity);

        assertThat(actual.getSource()).isEqualTo("LIBRA");
        assertThat(actual.getCaseId()).isEqualTo(CASE_ID);
        assertThat(actual.getUrn()).isEqualTo(URN);
        assertThat(actual.getHearingId()).isEqualTo("HEARING_ID");
        assertThat(actual.getCaseNo()).isEqualTo(CASE_NO);
        assertThat(actual.getHearingEventType()).isEqualTo("ConfirmedOrUpdated");
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
                .offender(Offender.builder().pnc(OFFENDER_PNC).build())
                .offences(List.of(OffenceRequestResponse.builder()
                                .act("act2")
                                .judicialResults(Collections.emptyList())
                                .build(),
                        OffenceRequestResponse.builder()
                                .act("act")
                                .offenceSummary("summary")
                                .offenceTitle("title")
                                .listNo(11)
                                .judicialResults(Collections.emptyList())
                                .build()
                ))
                .build());
        assertThat(actual.getDefendants().get(1).getDefendantId()).isEqualTo("DEFENDANT_ID_2");
        // offences should be sorted by sequence number
        assertThat(actual.getDefendants().get(0).getOffences().get(0).getAct()).isEqualTo("act2");
        assertThat(actual.getDefendants().get(0).getOffences().get(1).getAct()).isEqualTo("act");
    }

    @Test
    void givenOffencesWithJudicialResults_whenAsEntity_thenReturn() {

        final var defendant = buildDefendant("M");

        final var request = ExtendedHearingRequestResponse.builder()
                .defendants(List.of(defendant))
                .build();

        final var hearingEntity = request.asHearingEntity();

        assertThat(hearingEntity.getHearingDefendants()).hasSize(1);

        final var hearingDefendantEntity = hearingEntity.getHearingDefendants().get(0);
        assertThat(hearingDefendantEntity.getHearing()).isSameAs(hearingEntity);
        assertThat(hearingDefendantEntity.getDefendantId()).isSameAs(DEFENDANT_ID);

        final var offences = hearingDefendantEntity.getOffences();
        assertThat(offences).hasSize(2);
        assertThat(offences.get(0).getJudicialResults()).isNotEmpty();
        assertThat(offences.get(0).getJudicialResults().get(0).isConvictedResult()).isEqualTo(false);
        assertThat(offences.get(0).getJudicialResults().get(0).getLabel()).isEqualTo("label");
        assertThat(offences.get(0).getJudicialResults().get(0).getJudicialResultTypeId()).isEqualTo("judicialResultTypeId");
        assertThat(offences.get(1).getJudicialResults()).isEmpty();
    }

    private HearingEntity buildEntity() {
        return HearingEntity.builder()
                .hearingId("HEARING_ID")
                .hearingEventType(HearingEventType.CONFIRMED_OR_UPDATED)
                .courtCase(CourtCaseEntity.builder()
                        .sourceType(SourceType.LIBRA)
                        .caseId(CASE_ID)
                        .caseNo(CASE_NO)
                        .urn(URN)
                        .build())
                .hearingDays(List.of(HearingDayEntity.builder()
                                .courtCode(COURT_CODE)
                                .courtRoom(COURT_ROOM)
                                .day(LocalDate.of(2021, 10, 5))
                                .time(LocalTime.of(15, 15, 15))
                                .listNo("1")
                                .build(),
                        HearingDayEntity.builder()
                                .courtCode(COURT_CODE)
                                .listNo("2")
                                .build()
                ))
                .hearingDefendants(List.of(HearingDefendantEntity.builder()

                                .defendant(DefendantEntity.builder()
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
                                                .probationStatus(OffenderProbationStatus.CURRENT)
                                                .pnc(OFFENDER_PNC)
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
                                        .build())
                                .offences(List.of(OffenceEntity.builder()
                                                .act("act")
                                                .summary("summary")
                                                .title("title")
                                                .sequence(2)
                                                .listNo(11)
                                                .build(),
                                        OffenceEntity.builder()
                                                .act("act2")
                                                .sequence(1)
                                                .build()
                                ))
                                .build(),
                        HearingDefendantEntity.builder()
                                .defendant(DefendantEntity.builder()
                                        .defendantId("DEFENDANT_ID_2")
                                        .build())
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
                .phoneNumber(DEFENDANT_PHONE_NUMBER)
                .preSentenceActivity(Boolean.TRUE)
                .previouslyKnownTerminationDate(LocalDate.of(2021, Month.MARCH, 20))
                .probationStatus("CURRENT")
                .sex(Sex.fromString(sex).name())
                .suspendedSentenceOrder(Boolean.TRUE)
                .type(DefendantType.PERSON)
                .defendantId(DEFENDANT_ID)
                .offender(Offender.builder().pnc(OFFENDER_PNC).build())
                .offences(List.of(OffenceRequestResponse.builder()
                                .offenceTitle("TITLE1")
                                .offenceSummary("SUMMARY1")
                                .act("ACT1")
                                .listNo(10)
                                .judicialResults(List.of(buildJudicialResult()))
                                .build(),
                        OffenceRequestResponse.builder()
                                .offenceTitle("TITLE2")
                                .offenceSummary("SUMMARY2")
                                .act("ACT2")
                                .listNo(20)
                                .build()))
                .build();
    }

    private JudicialResult buildJudicialResult() {
        return JudicialResult.builder()
                .isConvictedResult(false)
                .label("label")
                .judicialResultTypeId("judicialResultTypeId")
                .build();
    }

}
