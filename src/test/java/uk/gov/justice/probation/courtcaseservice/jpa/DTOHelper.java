package uk.gov.justice.probation.courtcaseservice.jpa;


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
    public static final PhoneNumberEntity DEFENDANT_PHONE_NUMBER_ENTITY = PhoneNumberEntity.builder().home("07000000013").work("07000000014").mobile("07000000015").build();
    public static final String PROBATION_STATUS = "Previously known";
    public static final String NATIONALITY_1 = "British";
    public static final String NATIONALITY_2 = "Polish";
    public static final SourceType SOURCE = SourceType.COMMON_PLATFORM;

    public static final String OFFENCE_TITLE = "OFFENCE TITLE";
    public static final String OFFENCE_SUMMARY = "OFFENCE SUMMARY";
    public static final String OFFENCE_ACT = "OFFENCE ACT";

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

    public static HearingDTO aHearingDTOWithHearingId(String caseId, String hearingId, String defendantId) {
        final var hearingDTO = populateBasics(CRN, hearingId, defendantId)
                .hearingId(hearingId)
                .hearingEventType(HearingEventType.UNKNOWN)
                .hearingType("Unknown")
                .hearingDays(listOf(aHearingDayDTO()))
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

    private static HearingDefendantDTO aHearingDefendantDTO(AddressPropertiesEntity defendantAddress, NamePropertiesEntity name, String defendantId, String crn) {
        DefendantDTO defendant = aDefendantDTO(defendantAddress, name, defendantId, crn);
        final HearingDefendantDTO hearingDefendant = HearingDefendantDTO.builder()
                .defendantId(defendantId)
                .defendant(defendant)
                .hearing(aHearingDTO())
                .offences(getMutableList(List.of(aDefendantOffence())))
                .build();
        return hearingDefendant;
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

    public static HearingDayDTO aHearingDayDTO() {
        return aHearingDayDTO(SESSION_START_TIME);
    }

    public static HearingDayDTO aHearingDayDTO(LocalDateTime sessionStartTime) {
        return HearingDayDTO.builder()
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
}
