package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import uk.gov.justice.probation.courtcaseservice.controller.model.PhoneNumber;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EntityHelper {

    public static final String CASE_ID = "ac24a1be-939b-49a4-a524-21a3d228f8bc";
    public static final String HEARING_ID = "75e63d6c-5487-4244-a5bc-7cf8a38992db";
    public static final String COURT_CODE = "B10JQ";
    public static final String CRN = "X340906";
    public static final String URN = "URN001";
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

    public static final String DEFENDANT_ID2 = "45b969d6-b238-4573-8f89-c40250e5f7fe";

    public static final String PNC = "PNC";
    public static final String OFFENDER_PNC = "OFFENDER_PNC";
    public static final String CRO = "CRO/12334";
    public static final String DEFENDANT_SEX = "M";
    public static final PhoneNumberEntity DEFENDANT_PHONE_NUMBER_ENTITY = PhoneNumberEntity.builder().home("07000000013").work("07000000014").mobile("07000000015").build();
    public static final PhoneNumber DEFENDANT_PHONE_NUMBER = PhoneNumber.builder().home("07000000013").work("07000000014").mobile("07000000015").build();
    public static final String PROBATION_STATUS = "Previously known";
    public static final String NO_RECORD_DESCRIPTION = "No record";
    public static final String NATIONALITY_1 = "British";
    public static final String NATIONALITY_2 = "Polish";
    public static final SourceType SOURCE = SourceType.COMMON_PLATFORM;

    public static final String OFFENCE_TITLE = "OFFENCE TITLE";
    public static final String OFFENCE_SUMMARY = "OFFENCE SUMMARY";
    public static final String OFFENCE_ACT = "OFFENCE ACT";
    public static final Long OFFENDER_ID = 199L;

    public static final String PERSON_ID = "d1eefed2-04df-11ec-b2d8-0242ac130002";

    public static final String CASE_URN = "case-urn";


    public static <E> ArrayList<E> getMutableList(List<E> mutableList) {
        return new ArrayList<>(mutableList);
    }

    public static HearingEntity aHearingEntity() {
        return aHearingEntityWithHearingId(CASE_ID, HEARING_ID, DEFENDANT_ID);
    }

    public static HearingEntity aHearingEntity(String caseId) {
        return aHearingEntityWithHearingId(caseId, HEARING_ID, DEFENDANT_ID);
    }

    public static void refreshMappings(HearingEntity hearingEntity) {
        hearingEntity.getHearingDefendants().forEach(hearingDefendantEntity -> {
            hearingDefendantEntity.setHearing(hearingEntity);
            hearingDefendantEntity.getOffences().forEach(offenceEntity -> offenceEntity.setHearingDefendant(hearingDefendantEntity));
        });

        hearingEntity.getHearingDays().forEach(hearingDay -> hearingDay.setHearing(hearingEntity));
        Optional.ofNullable(hearingEntity.getCourtCase().getHearings())
            .ifPresent(hearings -> hearings.forEach(hearingEntity1 -> hearingEntity1.setCourtCase(hearingEntity.getCourtCase())));
    }

    public static void refreshMappings(CourtCaseEntity courtCaseEntity) {
        courtCaseEntity.getHearings().forEach(hearingEntity -> {
            hearingEntity.setCourtCase(courtCaseEntity);
            refreshMappings(hearingEntity);
        });
    }

    public static HearingEntity aHearingEntityWithHearingId(String caseId, String hearingId, String defendantId) {
        final var hearingEntity = populateBasics(CRN, hearingId, defendantId)
                .hearingId(hearingId)
                .hearingEventType(HearingEventType.UNKNOWN)
                .hearingType("Unknown")
                .courtCase(CourtCaseEntity.builder()
                        .caseId(caseId)
                        .caseNo(CASE_NO)
                        .sourceType(SOURCE)
                        .urn(CASE_URN)
                        .hearings(new ArrayList<>())
                        .build())
                .build();

        hearingEntity.getHearingDefendants()
                .forEach(hearingDefendant -> hearingDefendant.setHearing(hearingEntity));
        hearingEntity.getHearingDays()
                .forEach(hearingDay -> hearingDay.setHearing(hearingEntity));
        hearingEntity.getCourtCase().addHearing(hearingEntity);
        return hearingEntity;
    }

    public static HearingEntity aHearingEntityWithCrn(String crn) {
        return aHearingEntityWithCrnAndCaseIdAndHearingId(crn, CASE_ID, DEFENDANT_ID);
    }

    public static HearingEntity aHearingEntityWithCrnAndCaseIdAndHearingId(String crn, String caseId, String defendantId) {
        return populateBasics(crn, HEARING_ID, defendantId)
                .courtCase(CourtCaseEntity.builder()
                        .caseId(caseId)
                        .caseNo(CASE_NO)
                        .sourceType(SOURCE)
                        .build())
                .build();
    }

    public static HearingEntity aHearingEntity(String crn, String caseNo) {
        return aHearingEntity(crn, caseNo, getMutableList(List.of(aHearingDefendantEntity(DEFENDANT_ID, crn))));
    }

    public static HearingEntity aHearingEntity(String crn, String caseNo, List<HearingDefendantEntity> defendants) {
        return populateBasics(crn)
                .courtCase(CourtCaseEntity.builder()
                        .caseId(CASE_ID)
                        .caseNo(caseNo)
                        .urn(URN)
                        .sourceType(SOURCE)
                        .build())
                .hearingDefendants(defendants)
                .build();
    }

    public static HearingDefendantEntity aHearingDefendantEntity() {
        return aHearingDefendantEntity(DEFENDANT_ADDRESS, NAME);
    }

    public static HearingDefendantEntity aHearingDefendantEntity(AddressPropertiesEntity address, NamePropertiesEntity name) {
        return aHearingDefendantEntity(address, name, DEFENDANT_ID, CRN);
    }

    public static HearingDefendantEntity aHearingDefendantEntity(NamePropertiesEntity name) {
        return aHearingDefendantEntity(DEFENDANT_ADDRESS, name);
    }

    public static HearingDefendantEntity aHearingDefendantEntity(AddressPropertiesEntity address) {
        return aHearingDefendantEntity(address, NAME);
    }

    public static HearingDefendantEntity aHearingDefendantEntity(String defendantId) {
        return aHearingDefendantEntity(DEFENDANT_ADDRESS, NAME, defendantId, CRN);
    }

    public static HearingDefendantEntity aHearingDefendantEntity(String defendantId, String crn) {
        return aHearingDefendantEntity(DEFENDANT_ADDRESS, NAME, defendantId, crn);
    }

    private static HearingDefendantEntity aHearingDefendantEntity(AddressPropertiesEntity defendantAddress, NamePropertiesEntity name, String defendantId, String crn) {
        DefendantEntity defendant = aDefendantEntity(defendantAddress, name, defendantId, crn);
        final HearingDefendantEntity hearingDefendant = HearingDefendantEntity.builder()
                .defendantId(defendantId)
                .defendant(defendant)
                .offences(getMutableList(List.of(aDefendantOffence())))
                .notes(new ArrayList<>())
                .build();
        hearingDefendant.getOffences()
                .forEach(offenceEntity -> offenceEntity.setHearingDefendant(hearingDefendant));
        defendant.addHearingDefendant(hearingDefendant);
        return hearingDefendant;
    }

    public static DefendantEntity aDefendantEntity(String defendantId, String crn) {
        return aDefendantEntity(DEFENDANT_ADDRESS, NAME, defendantId, crn);
    }

    public static DefendantEntity aDefendantEntity() {
        return aDefendantEntity(DEFENDANT_ID, CRN);
    }

    private static DefendantEntity aDefendantEntity(AddressPropertiesEntity defendantAddress, NamePropertiesEntity name, String defendantId, String crn) {
        return DefendantEntity.builder()
                .name(name)
                .defendantName(name.getFullName())
                .offender(anOffender(crn))
                .crn(crn)
                .cro(CRO)
                .pnc(PNC)
                .type(DefendantType.PERSON)
                .address(defendantAddress)
                .dateOfBirth(DEFENDANT_DOB)
                .sex(Sex.fromString(DEFENDANT_SEX))
                .nationality1(NATIONALITY_1)
                .nationality2(NATIONALITY_2)
                .defendantId(defendantId)
                .phoneNumber(DEFENDANT_PHONE_NUMBER_ENTITY)
                .personId(PERSON_ID)
                .hearingDefendants(getMutableList(List.of()))
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
                        .build())
                .orElse(null);
    }

    public static HearingDayEntity aHearingDayEntity() {
        return aHearingDayEntity(SESSION_START_TIME);

    }

    public static HearingDayEntity aHearingDayEntity(LocalDateTime sessionStartTime) {
        return HearingDayEntity.builder()
                .day(sessionStartTime.toLocalDate())
                .time(sessionStartTime.toLocalTime())
                .courtRoom(COURT_ROOM)
                .courtCode(COURT_CODE)
                .build();
    }

    private static HearingEntity.HearingEntityBuilder populateBasics(String crn) {
        return populateBasics(crn, HEARING_ID, DEFENDANT_ID);
    }
    private static HearingEntity.HearingEntityBuilder populateBasics(String crn, String hearingId, String defendantId, HearingEventType hearingEventType) {
        var defendant = aHearingDefendantEntity(defendantId, crn);
        return HearingEntity.builder()
            .hearingId(hearingId)
            .hearingEventType(hearingEventType)
            .deleted(false)
            .firstCreated(LocalDateTime.now())
            .hearingDefendants(getMutableList(List.of(defendant)))
            .hearingDays(getMutableList(List.of(aHearingDayEntity())));
    }
    private static HearingEntity.HearingEntityBuilder populateBasics(String crn, String hearingId, String defendantId) {
        return populateBasics(crn, hearingId, defendantId, HearingEventType.UNKNOWN);
    }

    public static OffenceEntity aDefendantOffence() {
        return aDefendantOffence(OFFENCE_TITLE, 1);
    }

    public static OffenceEntity aDefendantOffence(String title, Integer seq) {
        final var offenceEntity = OffenceEntity.builder()
                .summary(OFFENCE_SUMMARY)
                .title(title)
                .act(OFFENCE_ACT)
                .offenceCode("EFG001")
                .sequence(seq)
                .plea(PleaEntity.builder().build())
                .verdict(VerdictEntity.builder().build())
                .judicialResults(getMutableList(List.of(JudicialResultEntity.builder()
                        .isConvictedResult(false)
                        .label("label")
                        .judicialResultTypeId("judicialResultTypeId")
                        .resultText("resultText")
                        .build())))
                .build();

        offenceEntity.getJudicialResults().forEach(judicialResultEntity -> judicialResultEntity.setOffence(offenceEntity));
        return offenceEntity;
    }


    public static HearingDefendantEntity aHearingDefendant(NamePropertiesEntity name) {
        final var offender = anOffender(CRN);
        return aHearingDefendant(name, offender);
    }

    public static HearingDefendantEntity aHearingDefendant(NamePropertiesEntity name, OffenderEntity offender) {
        return aHearingDefendant(name, offender, null);
    }

    public static HearingDefendantEntity aHearingDefendant(NamePropertiesEntity name, OffenderEntity offender, Long id) {
        return aHearingDefendant(name, offender, id, UUID.randomUUID().toString());
    }

    public static HearingDefendantEntity aHearingDefendant(NamePropertiesEntity name, OffenderEntity offender, Long id, String defendantId) {
        return HearingDefendantEntity.builder()
                .id(id)
                .defendantId(defendantId)
                .defendant(DefendantEntity.builder()
                        .defendantId(defendantId)
                        .name(name)
                        .defendantName(name.getFullName())
                        .offender(offender)
                        .crn(CRN)
                        .cro(CRO)
                        .pnc(PNC)
                        .type(DefendantType.PERSON)
                        .address(DEFENDANT_ADDRESS)
                        .dateOfBirth(DEFENDANT_DOB)
                        .sex(Sex.fromString(DEFENDANT_SEX))
                        .nationality1(NATIONALITY_1)
                        .nationality2(NATIONALITY_2)
                        .build())
                .offences(getMutableList(List.of(aDefendantOffence())))
                .build();
    }

    public static HearingDefendantEntity aHearingDefendantEntity(long id) {
        return aHearingDefendant(NAME, anOffender(CRN), id);
    }

    public static HearingDefendantEntity aHearingDefendantEntity(long id, String defendantId) {
        return aHearingDefendant(NAME, anOffender(CRN), id, defendantId);
    }


    public static HearingEntity aHearingEntityWithJudicialResults(String crn, String caseNo) {
        var hearingEntity = aHearingEntity(crn, caseNo, getMutableList(List.of(aHearingDefendantEntityWithJudicialResults(DEFENDANT_ID, crn))));

        hearingEntity.getHearingDefendants()
                .forEach(hearingDefendant -> hearingDefendant.setHearing(hearingEntity));

        hearingEntity.getHearingDays()
                .forEach(hearingDay -> hearingDay.setHearing(hearingEntity));
        return hearingEntity;
    }

    public static HearingDefendantEntity aHearingDefendantEntityWithJudicialResults(String defendantId, String crn) {
        return aHearingDefendantEntityWithJudicialResults(DEFENDANT_ADDRESS, NAME, defendantId, crn);
    }

    private static HearingDefendantEntity aHearingDefendantEntityWithJudicialResults(AddressPropertiesEntity defendantAddress, NamePropertiesEntity name, String defendantId, String crn) {
        final HearingDefendantEntity hearingDefendant = HearingDefendantEntity.builder()
                .defendantId(defendantId)
                .defendant(aDefendantEntity(defendantAddress, name, defendantId, crn))
                .offences(getMutableList(List.of(aDefendantOffenceWithJudicialResults())))
                .build();


        hearingDefendant.getOffences()
                .forEach(offenceEntity -> offenceEntity.setHearingDefendant(hearingDefendant));
        return hearingDefendant;
    }

    public static OffenceEntity aDefendantOffenceWithJudicialResults() {
        return aDefendantOffenceWithJudicialResults(OFFENCE_TITLE, 1);
    }

    public static OffenceEntity aDefendantOffenceWithJudicialResults(String title, Integer seq) {
        final OffenceEntity offenceEntity = OffenceEntity.builder()
                .summary(OFFENCE_SUMMARY)
                .title(title)
                .act(OFFENCE_ACT)
                .sequence(seq)
                .plea(aPleaEntity("value 1",LocalDate.now()))
                .verdict(aVerdictEntity("type 1", LocalDate.now()))
                .judicialResults(getMutableList(List.of(aJudicialResultEntity("id1"), aJudicialResultEntity("id3"), aJudicialResultEntity("id2"), aJudicialResultEntity("id4"))))
                .build();

        offenceEntity.getJudicialResults().forEach(judicialResultEntity -> judicialResultEntity.setOffence(offenceEntity));

        return offenceEntity;
    }

    public static JudicialResultEntity aJudicialResultEntity(String label) {
        return JudicialResultEntity.builder()
                .isConvictedResult(false)
                .label(label)
                .judicialResultTypeId("judicialResultTypeId")
                .resultText("resultText")
                .build();
    }

    public static PleaEntity aPleaEntity(String pleaValue, LocalDate pleaDate){
        return PleaEntity.builder()
                .value(pleaValue)
                .date(pleaDate)
                .build();
    }

    public static VerdictEntity aVerdictEntity(String typeDescription, LocalDate  verdictDate){
        return VerdictEntity.builder()
                .typeDescription(typeDescription)
                .date(verdictDate)
                .build();
    }
}
