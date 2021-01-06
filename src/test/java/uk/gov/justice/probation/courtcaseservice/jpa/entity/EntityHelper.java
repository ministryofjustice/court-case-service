package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EntityHelper {

    public static final String CASE_ID = "123";
    public static final String COURT_CODE = "B10JQ";
    public static final String CRN  = "X340906";
    public static final String CASE_NO = "1001";
    public static final String DEFENDANT_NAME = "Gordon BENNETT";
    public static final String COURT_ROOM = "1";
    public static final LocalDateTime SESSION_START_TIME = LocalDateTime.of(2020, 2, 26, 9, 0);
    public static final boolean SUSPENDED_SENTENCE = true;
    public static final boolean BREACH = true;
    public static final LocalDate TERMINATION_DATE = LocalDate.of(2020, 2, 27);
    public static final String PNC = "PNC";
    public static final String LIST_NO = "1st";
    public static final AddressPropertiesEntity DEFENDANT_ADDRESS = new AddressPropertiesEntity("27", "Elm Place", "AB21 3ES", "Bangor", null, null);
    public static final LocalDate DEFENDANT_DOB = LocalDate.of(1958, 12, 14);
    public static final String DEFENDANT_SEX = "M";
    public static final String PROBATION_STATUS = "Previously known";
    public static final String NATIONALITY_1 = "British";
    public static final String NATIONALITY_2 = "Polish";

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

    private static CourtCaseEntity.CourtCaseEntityBuilder populateBasics() {
        return CourtCaseEntity.builder()
            .caseId(CASE_ID)
            .courtCode(COURT_CODE)
            .courtRoom(COURT_ROOM)
            .sessionStartTime(SESSION_START_TIME)
            .probationStatus(PROBATION_STATUS)
            .previouslyKnownTerminationDate(TERMINATION_DATE)
            .suspendedSentenceOrder(SUSPENDED_SENTENCE)
            .breach(BREACH)
            .defendantName(DEFENDANT_NAME)
            .name(NamePropertiesEntity.builder().forename1("Gordon").surname("BENNETT").build())
            .defendantAddress(DEFENDANT_ADDRESS)
            .defendantDob(DEFENDANT_DOB)
            .defendantSex(DEFENDANT_SEX)
            .defendantType(DefendantType.PERSON)
            .pnc(PNC)
            .cro("CRO/12334")
            .listNo(LIST_NO)
            .nationality1(NATIONALITY_1)
            .nationality2(NATIONALITY_2)
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
