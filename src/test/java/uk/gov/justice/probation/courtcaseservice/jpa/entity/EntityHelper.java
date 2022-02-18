package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    public static final String NO_RECORD_DESCRIPTION = "No record";
    public static final String NATIONALITY_1 = "British";
    public static final String NATIONALITY_2 = "Polish";
    public static final SourceType SOURCE = SourceType.COMMON_PLATFORM;

    public static final String OFFENCE_TITLE = "OFFENCE TITLE";
    public static final String OFFENCE_SUMMARY = "OFFENCE SUMMARY";
    public static final String OFFENCE_ACT = "OFFENCE ACT";
    public static final Long OFFENDER_ID = 199L;

    public static CourtCaseEntity aCourtCaseEntity(String caseId) {
        return populateBasics(CRN)
            .caseId(caseId)
            .caseNo(CASE_NO)
            .build();
    }

    public static CourtCaseEntity aCourtCaseEntityWithCrn(String crn) {
        return populateBasics(crn)
            .caseId(CASE_ID)
            .caseNo(CASE_NO)
            .build();
    }

    public static CourtCaseEntity aCourtCaseEntity(String crn, String caseNo) {
        return aCourtCaseEntity(crn, caseNo, List.of(aDefendantEntity(DEFENDANT_ID, crn)));
    }

    public static CourtCaseEntity aCourtCaseEntity(String crn, String caseNo, List<DefendantEntity> defendants) {
        return populateBasics(crn)
            .caseId(CASE_ID)
            .caseNo(caseNo)
            .defendants(defendants)
            .build();
    }

    public static CourtCaseEntity aCourtCaseEntity(String crn, String caseNo, String probationStatus) {
        return populateBasics(crn)
            .caseId(CASE_ID)
            .caseNo(caseNo)
            .build();
    }

    public static DefendantEntity aDefendantEntity() {
        return aDefendantEntity(DEFENDANT_ADDRESS, NAME);
    }

    public static DefendantEntity aDefendantEntity(AddressPropertiesEntity address, NamePropertiesEntity name) {
        return aDefendantEntity(address, name, DEFENDANT_ID, CRN);
    }

    public static DefendantEntity aDefendantEntity(NamePropertiesEntity name) {
        return aDefendantEntity(DEFENDANT_ADDRESS, name);
    }

    public static DefendantEntity aDefendantEntity(AddressPropertiesEntity address) {
        return aDefendantEntity(address, NAME);
    }

    public static DefendantEntity aDefendantEntity(String defendantId) {
        return aDefendantEntity(DEFENDANT_ADDRESS, NAME, defendantId, CRN);
    }

    public static DefendantEntity aDefendantEntity(String defendantId, String crn) {
        return aDefendantEntity(DEFENDANT_ADDRESS, NAME, defendantId, crn);
    }

    private static DefendantEntity aDefendantEntity(AddressPropertiesEntity defendantAddress, NamePropertiesEntity name, String defendantId, String crn) {
        return DefendantEntity.builder()
            .name(name)
            .defendantName(name.getFullName())
            .offender(anOffender(crn))
            .cro(CRO)
            .pnc(PNC)
            .type(DefendantType.PERSON)
            .address(defendantAddress)
            .dateOfBirth(DEFENDANT_DOB)
            .sex(Sex.fromString(DEFENDANT_SEX))
            .nationality1(NATIONALITY_1)
            .nationality2(NATIONALITY_2)
            .defendantId(defendantId)
            .offences(List.of(aDefendantOffence()))
            .build();
    }

    public static OffenderEntity anOffender(String crn) {
        return Optional.ofNullable(crn)
            .map(str -> OffenderEntity.builder()
                    .crn(str)
                    .awaitingPsr(AWAITING_PSR)
                    .breach(BREACH)
                    .preSentenceActivity(PRE_SENTENCE_ACTIVITY)
                    .previouslyKnownTerminationDate(TERMINATION_DATE)
                    .probationStatus(OffenderProbationStatus.of(PROBATION_STATUS))
                    .suspendedSentenceOrder(SUSPENDED_SENTENCE)
                    .id(OFFENDER_ID)
                    .build())
            .orElse(null);
    }

    public static HearingDayEntity aHearingEntity() {
        return aHearingEntity(SESSION_START_TIME);
    }

    public static HearingDayEntity aHearingEntity(LocalDateTime sessionStartTime) {
        return HearingDayEntity.builder()
            .listNo(LIST_NO)
            .day(sessionStartTime.toLocalDate())
            .time(sessionStartTime.toLocalTime())
            .courtRoom(COURT_ROOM)
            .courtCode(COURT_CODE)
            .build();
    }

    private static CourtCaseEntity.CourtCaseEntityBuilder populateBasics(String crn) {
        var defendant = aDefendantEntity(DEFENDANT_ID, crn);
        return CourtCaseEntity.builder()
            .sourceType(SOURCE)
            .deleted(false)
            .firstCreated(LocalDateTime.now())
            .defendants(List.of(defendant))
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
