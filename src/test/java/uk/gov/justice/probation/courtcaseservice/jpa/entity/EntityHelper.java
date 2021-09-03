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

    public static CourtCaseEntity aCourtCaseEntity(String crn) {
        return populateBasics()
            .crn(crn)
            .caseNo(CASE_NO)
            .build();
    }

    public static CourtCaseEntity aCourtCaseEntity(String crn, String caseNo) {
        return populateBasics()
            .crn(crn)
            .caseNo(caseNo)
            .build();
    }

    public static CourtCaseEntity aCourtCaseEntity(String crn, String caseNo, LocalDateTime sessionStartTime, String probationStatus) {
        return populateBasics()
            .crn(crn)
            .caseNo(caseNo)
            .sessionStartTime(sessionStartTime)
            .probationStatus(probationStatus)
            .build();
    }

    public static CourtCaseEntity aCourtCaseEntity(String crn, String caseNo, LocalDateTime sessionStartTime, String probationStatus, String caseId, String courtCode) {
        return populateBasics()
            .crn(crn)
            .caseNo(caseNo)
            .sessionStartTime(sessionStartTime)
            .probationStatus(probationStatus)
            .caseId(caseId)
            .courtCode(courtCode)
            .build();
    }

    public static DefendantEntity aDefendantEntity(AddressPropertiesEntity address) {
        return DefendantEntity.builder()
            .name(NAME)
            .defendantName(NAME.getFullName())
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
            .build();
    }

    public static HearingEntity aHearingEntity() {
        return HearingEntity.builder()
            .listNo(LIST_NO)
            .hearingDay(SESSION_START_TIME.toLocalDate())
            .hearingTime(SESSION_START_TIME.toLocalTime())
            .courtRoom(COURT_ROOM)
            .courtCode(COURT_CODE)
            .build();
    }

    private static CourtCaseEntity.CourtCaseEntityBuilder populateBasics() {
        return CourtCaseEntity.builder()
            .caseId(CASE_ID)
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
            .offences(List.of(anOffence()));
    }

    private static OffenceEntity anOffence() {
        return OffenceEntity.builder()
            .offenceSummary(OFFENCE_SUMMARY)
            .offenceTitle(OFFENCE_TITLE)
            .act(OFFENCE_ACT)
            .sequenceNumber(1)
            .build();
    }


}
