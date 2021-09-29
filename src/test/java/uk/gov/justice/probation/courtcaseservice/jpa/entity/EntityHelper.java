package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EntityHelper {

    public static final String CASE_ID = "ac24a1be-939b-49a4-a524-21a3d228f8bc";
    public static final String COURT_CODE = "B10JQ";
    public static final String CRN  = "X340906";
    public static final String CASE_NO = "1001";

    public static final String COURT_ROOM = "1";
    public static final LocalDateTime SESSION_START_TIME = LocalDateTime.of(2020, 2, 26, 9, 0);
    public static final boolean SUSPENDED_SENTENCE = true;
    public static final boolean BREACH = true;
    public static final boolean PRE_SENTENCE_ACTIVITY = true;
    public static final boolean AWAITING_PSR = false;
    public static final LocalDate TERMINATION_DATE = LocalDate.of(2020, 2, 27);
    public static final String LIST_NO = "1st";

    public static final NamePropertiesEntity NAME = NamePropertiesEntity.builder()
        .forename1("Gordon")
        .surname("BENNETT")
        .title("Mr")
        .build();
    public static final String DEFENDANT_NAME = NAME.getFullName();
    public static final AddressPropertiesEntity DEFENDANT_ADDRESS = new AddressPropertiesEntity("27", "Elm Place", "AB21 3ES", "Bangor", null, null);
    public static final LocalDate DEFENDANT_DOB = LocalDate.of(1958, 12, 14);
    public static final String DEFENDANT_ID = "d1eefed2-04df-11ec-b2d8-0242ac130002";
    public static final String PNC = "PNC";
    public static final String CRO = "CRO/12334";
    public static final String DEFENDANT_SEX = "M";
    public static final String PROBATION_STATUS = "Previously known";
    public static final String NATIONALITY_1 = "British";
    public static final String NATIONALITY_2 = "Polish";
    public static final SourceType SOURCE = SourceType.COMMON_PLATFORM;

    public static final String OFFENCE_TITLE = "OFFENCE TITLE";
    public static final String OFFENCE_SUMMARY = "OFFENCE SUMMARY";
    public static final String OFFENCE_ACT = "OFFENCE ACT";

    public static CourtCaseEntity aCourtCaseEntity(String caseId) {
        return populateBasics()
            .caseId(caseId)
            .caseNo(CASE_NO)
            .build();
    }

    public static CourtCaseEntity aCourtCaseEntityLinked(String crn) {
        return populateBasics()
            .crn(crn)
            .caseId(CASE_ID)
            .caseNo(CASE_NO)
            .build();
    }

    public static CourtCaseEntity aCourtCaseEntity(String crn, String caseNo) {

        return populateBasics()
            .crn(crn)
            .caseId(CASE_ID)
            .caseNo(caseNo)
            .defendants(List.of(aDefendantEntity()))
            .build();
    }

    public static CourtCaseEntity aCourtCaseEntityLinked(String crn, String caseNo, LocalDateTime sessionStartTime, String probationStatus) {
        return populateBasics()
            .caseId(CASE_ID)
            .crn(crn)
            .caseNo(caseNo)
            .sessionStartTime(sessionStartTime)
            .probationStatus(probationStatus)
            .build();
    }

    public static CourtCaseEntity aCourtCaseEntityLinked(String crn, String caseNo, LocalDateTime sessionStartTime, String probationStatus, String caseId, String courtCode) {
        return populateBasics()
            .crn(crn)
            .caseNo(caseNo)
            .sessionStartTime(sessionStartTime)
            .probationStatus(probationStatus)
            .caseId(caseId)
            .courtCode(courtCode)
            .build();
    }

    public static DefendantEntity aDefendantEntity() {
        return aDefendantEntity(DEFENDANT_ADDRESS, NAME);
    }

    public static DefendantEntity aDefendantEntity(AddressPropertiesEntity address, NamePropertiesEntity name) {
        return DefendantEntity.builder()
            .name(name)
            .defendantName(name.getFullName())
            .crn(CRN)
            .cro(CRO)
            .pnc(PNC)
            .type(DefendantType.PERSON)
            .address(address)
            .dateOfBirth(DEFENDANT_DOB)
            .sex(DEFENDANT_SEX)
            .nationality1(NATIONALITY_1)
            .nationality2(NATIONALITY_2)
            .defendantId(DEFENDANT_ID)
            .awaitingPsr(AWAITING_PSR)
            .breach(BREACH)
            .preSentenceActivity(PRE_SENTENCE_ACTIVITY)
            .previouslyKnownTerminationDate(TERMINATION_DATE)
            .probationStatus(PROBATION_STATUS)
            .suspendedSentenceOrder(SUSPENDED_SENTENCE)
            .offences(List.of(aDefendantOffence()))
            .build();
    }

    public static DefendantEntity aDefendantEntity(NamePropertiesEntity name) {
        return aDefendantEntity(DEFENDANT_ADDRESS, name);
    }

    public static DefendantEntity aDefendantEntity(AddressPropertiesEntity address) {
        return aDefendantEntity(address, NAME);
    }

    public static HearingEntity aHearingEntity() {
        return aHearingEntity(SESSION_START_TIME);
    }

    public static HearingEntity aHearingEntity(LocalDateTime sessionStartTime) {
        return HearingEntity.builder()
            .listNo(LIST_NO)
            .hearingDay(sessionStartTime.toLocalDate())
            .hearingTime(sessionStartTime.toLocalTime())
            .courtRoom(COURT_ROOM)
            .courtCode(COURT_CODE)
            .build();
    }

    public static OffenceEntity anOffence() {
        return OffenceEntity.builder()
            .offenceSummary(OFFENCE_SUMMARY)
            .offenceTitle(OFFENCE_TITLE)
            .act(OFFENCE_ACT)
            .sequenceNumber(1)
            .build();
    }

    private static CourtCaseEntity.CourtCaseEntityBuilder populateBasics() {
        var defendant = aDefendantEntity();
        return CourtCaseEntity.builder()
            .courtCode(COURT_CODE)
            .courtRoom(COURT_ROOM)
            .sessionStartTime(SESSION_START_TIME)
            .probationStatus(PROBATION_STATUS)
            .previouslyKnownTerminationDate(TERMINATION_DATE)
            .suspendedSentenceOrder(SUSPENDED_SENTENCE)
            .preSentenceActivity(PRE_SENTENCE_ACTIVITY)
            .breach(BREACH)
            .defendantName(DEFENDANT_NAME)
            .name(NAME)
            .defendantAddress(DEFENDANT_ADDRESS)
            .defendantDob(DEFENDANT_DOB)
            .defendantSex(DEFENDANT_SEX)
            .defendantType(DefendantType.PERSON)
            .pnc(PNC)
            .cro(CRO)
            .listNo(LIST_NO)
            .nationality1(NATIONALITY_1)
            .nationality2(NATIONALITY_2)
            .awaitingPsr(AWAITING_PSR)
            .sourceType(SOURCE)
            .deleted(false)
            .firstCreated(LocalDateTime.now())
            .defendants(List.of(defendant))
            .offences(List.of(anOffence()))
            .hearings(List.of(aHearingEntity()));
    }

    public static DefendantOffenceEntity aDefendantOffence() {
        return aDefendantOffence(OFFENCE_TITLE, 1);
    }

    public static DefendantOffenceEntity aDefendantOffence(String title, Integer seq) {
        return DefendantOffenceEntity.builder()
            .summary(OFFENCE_SUMMARY)
            .title(title)
            .act(OFFENCE_ACT)
            .sequence(seq)
            .build();
    }

}
