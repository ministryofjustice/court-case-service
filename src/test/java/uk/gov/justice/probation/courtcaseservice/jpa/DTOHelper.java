package uk.gov.justice.probation.courtcaseservice.jpa;


import uk.gov.justice.probation.courtcaseservice.controller.model.PhoneNumber;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.*;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hibernate.internal.util.collections.CollectionHelper.listOf;

public class DTOHelper {

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

    public static HearingDTO aHearingDTO() {
        return aHearingDTOWithHearingId(CASE_ID, HEARING_ID, DEFENDANT_ID);
    }

    public static HearingDTO aHearingDTO(String caseId) {
        return aHearingDTOWithHearingId(caseId, HEARING_ID, DEFENDANT_ID);
    }

    public static void refreshMappings(HearingDTO hearingDTO) {
        hearingDTO.getHearingDays().forEach(hearingDay -> hearingDay.setHearing(hearingDTO));
    }

    public static HearingDTO aHearingDTOWithHearingId(String caseId, String hearingId, String defendantId) {
        final var hearingDTO = populateBasics(CRN, hearingId, defendantId)
                .hearingId(hearingId)
                .hearingEventType(HearingEventType.UNKNOWN)
                .hearingType("Unknown")
                .courtCase(CourtCaseDTO.builder()
                        .caseId(caseId)
                        .caseNo(CASE_NO)
                        .sourceType(SOURCE)
                        .urn(CASE_URN)
                        .build())
                .build();

        hearingDTO.getHearingDays()
                .forEach(hearingDay -> hearingDay.setHearing(hearingDTO));
        return hearingDTO;
    }

    public static HearingDTO aHearingEntityWithCrn(String crn) {
        return aHearingEntityWithCrnAndCaseIdAndHearingId(crn, CASE_ID, DEFENDANT_ID);
    }

    public static HearingDTO aHearingEntityWithCrnAndCaseIdAndHearingId(String crn, String caseId, String defendantId) {
        return populateBasics(crn, HEARING_ID, defendantId)
                .courtCase(CourtCaseDTO.builder()
                        .caseId(caseId)
                        .caseNo(CASE_NO)
                        .sourceType(SOURCE)
                        .build())
                .build();
    }

    public static HearingDTO aHearingDTO(String crn, String caseNo) {
        return aHearingDTO(crn, caseNo, getMutableList(List.of(aHearingDefendantDTO(DEFENDANT_ID, crn))));
    }

    public static HearingDTO aHearingDTO(String crn, String caseNo, List<HearingDefendantDTO> defendants) {
        return populateBasics(crn)
                .courtCase(CourtCaseDTO.builder()
                        .caseId(CASE_ID)
                        .caseNo(caseNo)
                        .urn(URN)
                        .sourceType(SOURCE)
                        .build())
                .build();
    }

    public static HearingDefendantDTO aHearingDefendantDTO() {
        return aHearingDefendantDTO(DEFENDANT_ADDRESS, NAME);
    }

    public static HearingDefendantDTO aHearingDefendantDTO(AddressPropertiesEntity address, NamePropertiesEntity name) {
        return aHearingDefendantDTO(address, name, DEFENDANT_ID, CRN);
    }

    public static HearingDefendantDTO aHearingDefendantDTO(NamePropertiesEntity name) {
        return aHearingDefendantDTO(DEFENDANT_ADDRESS, name);
    }

    public static HearingDefendantDTO aHearingDefendantDTO(AddressPropertiesEntity address) {
        return aHearingDefendantDTO(address, NAME);
    }

    public static HearingDefendantDTO aHearingDefendantDTO(String defendantId) {
        return aHearingDefendantDTO(DEFENDANT_ADDRESS, NAME, defendantId, CRN);
    }

    public static HearingDefendantDTO aHearingDefendantDTO(String defendantId, String crn) {
        return aHearingDefendantDTO(DEFENDANT_ADDRESS, NAME, defendantId, crn);
    }

    public static HearingDefendantDTO aHearingDefendantDTOWithCrn(Long id, String crn) {
        return aHearingDefendantDTOWithId(DEFENDANT_ID, crn, id);
    }
    private static HearingDefendantDTO aHearingDefendantDTO(AddressPropertiesEntity defendantAddress, NamePropertiesEntity name, String defendantId, String crn) {
        DefendantDTO defendant = aDefendantDTO(defendantAddress, name, defendantId, crn);
        final HearingDefendantDTO hearingDefendant = HearingDefendantDTO.builder()
                .defendantId(defendantId)
                .defendant(defendant)
                .offences(getMutableList(List.of(aDefendantOffence())))
                .build();
        return hearingDefendant;
    }

    private static HearingDefendantDTO aHearingDefendantDTOWithId(String defendantId, String crn, Long id) {
        DefendantDTO defendant = aDefendantDTO(defendantId, crn);
        final HearingDefendantDTO hearingDefendant = HearingDefendantDTO.builder()
                .id(id)
                .defendantId(defendantId)
                .defendant(defendant)
                .offences(getMutableList(List.of(aDefendantOffence())))
                .hearingOutcome(aHearingOutcomeDTO())
                .build();
        return hearingDefendant;
    }

    public static HearingOutcomeDTO aHearingOutcomeDTO() {
        return HearingOutcomeDTO.builder()
                .outcomeType("ADJOURNED")
                .outcomeDate(LocalDateTime.of(2020, 5, 1, 0, 0))
                .resultedDate(LocalDateTime.of(2020, 5, 1, 0, 0))
                .state("IN_PROGRESS")
                .assignedTo("John Doe")
                .created(LocalDateTime.of(2024, 1, 1, 0, 0)).build();
    }

    public static DefendantDTO aDefendantDTO(String defendantId, String crn) {
        return aDefendantDTO(DEFENDANT_ADDRESS, NAME, defendantId, crn);
    }

    public static DefendantDTO aDefendantDTO() {
        return aDefendantDTO(DEFENDANT_ID, CRN);
    }

    private static DefendantDTO aDefendantDTO(AddressPropertiesEntity defendantAddress, NamePropertiesEntity name, String defendantId, String crn) {
        return DefendantDTO.builder()
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
                .build();
    }

    private static HearingNoteEntity aHearingNoteEntity(Boolean draft) {
        return HearingNoteEntity.builder()
                .note("This is a fake note")
                .author("Note Taker")
                .createdByUuid("created-by-uuid")
                .hearingId("UUID")
                .draft(draft)
                .created(LocalDateTime.of(2024, 1, 1,0, 0))
                .build();
    }

    public static OffenderDTO anOffender(String crn) {
        return Optional.ofNullable(crn)
                .map(str -> OffenderDTO.builder()
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

    private static HearingDTO.HearingDTOBuilder populateBasics(String crn) {
        return populateBasics(crn, HEARING_ID, DEFENDANT_ID);
    }
    private static HearingDTO.HearingDTOBuilder populateBasics(String crn, String hearingId, String defendantId, HearingEventType hearingEventType) {
        var defendant = aHearingDefendantDTO(defendantId, crn);
        return HearingDTO.builder()
                .hearingId(hearingId)
                .hearingEventType(hearingEventType)
                .firstCreated(LocalDateTime.now());
    }
    private static HearingDTO.HearingDTOBuilder populateBasics(String crn, String hearingId, String defendantId) {
        return populateBasics(crn, hearingId, defendantId, HearingEventType.UNKNOWN);
    }

    public static OffenceDTO aDefendantOffence() {
        return aDefendantOffence(OFFENCE_TITLE, 1);
    }

    public static OffenceDTO aDefendantOffence(String title, Integer seq) {
        final var offenceDTO = OffenceDTO.builder()
                .summary(OFFENCE_SUMMARY)
                .title(title)
                .act(OFFENCE_ACT)
                .offenceCode("EFG001")
                .sequence(seq)
                .plea(PleaEntity.builder().build())
                .verdict(VerdictEntity.builder().build())
                .build();

        return offenceDTO;
    }


    public static HearingDefendantDTO aHearingDefendant(NamePropertiesEntity name) {
        final var offender = anOffender(CRN);
        return aHearingDefendant(name, offender);
    }

    public static HearingDefendantDTO aHearingDefendant(NamePropertiesEntity name, OffenderDTO offender) {
        return aHearingDefendant(name, offender, null);
    }

    public static HearingDefendantDTO aHearingDefendant(NamePropertiesEntity name, OffenderDTO offender, Long id) {
        return aHearingDefendant(name, offender, id, UUID.randomUUID().toString());
    }

    public static HearingDefendantDTO aHearingDefendant(NamePropertiesEntity name, OffenderDTO offender, Long id, String defendantId) {
        return HearingDefendantDTO.builder()
                .id(id)
                .defendantId(defendantId)
                .defendant(DefendantDTO.builder()
                        .defendantId(defendantId)
                        .name(name)
                        .defendantName(name.getFullName())
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

    public static HearingDefendantDTO aHearingDefendantDTO(long id) {
        return aHearingDefendant(NAME, anOffender(CRN), id);
    }

    public static HearingDefendantDTO aHearingDefendantDTO(long id, String defendantId) {
        return aHearingDefendant(NAME, anOffender(CRN), id, defendantId);
    }

    public static HearingDefendantDTO aHearingDefendantDTOWithJudicialResults(String defendantId, String crn) {
        return aHearingDefendantDTOWithJudicialResults(DEFENDANT_ADDRESS, NAME, defendantId, crn);
    }

    private static HearingDefendantDTO aHearingDefendantDTOWithJudicialResults(AddressPropertiesEntity defendantAddress, NamePropertiesEntity name, String defendantId, String crn) {
        final HearingDefendantDTO hearingDefendant = HearingDefendantDTO.builder()
                .defendantId(defendantId)
                .defendant(aDefendantDTO(defendantAddress, name, defendantId, crn))
                .offences(getMutableList(List.of(aDefendantOffenceWithJudicialResults())))
                .build();

        return hearingDefendant;
    }

    public static OffenceDTO aDefendantOffenceWithJudicialResults() {
        return aDefendantOffenceWithJudicialResults(OFFENCE_TITLE, 1);
    }

    public static OffenceDTO aDefendantOffenceWithJudicialResults(String title, Integer seq) {
        final OffenceDTO offenceDTO = OffenceDTO.builder()
                .summary(OFFENCE_SUMMARY)
                .title(title)
                .act(OFFENCE_ACT)
                .sequence(seq)
                .plea(aPleaEntity("value 1",LocalDate.now()))
                .verdict(aVerdictEntity("type 1", LocalDate.now()))
                .build();

        return offenceDTO;
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

    public static CaseCommentEntity aCaseCommentEntity() {
        return CaseCommentEntity.builder()
                .caseId("5678")
                .comment("Some comment")
                .author("Some author")
                .created(LocalDateTime.of(2024, 5, 22, 12, 0))
                .createdBy("Test User")
                .lastUpdated(LocalDateTime.of(2024, 5, 22, 12, 30))
                .lastUpdatedBy("Test User")
                .build();
    }

    public static CourtCaseDTO aCourtCaseDTO() {
        return CourtCaseDTO.builder()
                .caseId("5678")
                .sourceType(SourceType.LIBRA)
                .caseNo("222333")
                .created(LocalDateTime.of(2024, 5, 22, 12, 0))
                .createdBy("Test User")
                .lastUpdated(LocalDateTime.of(2024, 5, 22, 12, 30))
                .lastUpdatedBy("Test User")
                .build();
    }
}
