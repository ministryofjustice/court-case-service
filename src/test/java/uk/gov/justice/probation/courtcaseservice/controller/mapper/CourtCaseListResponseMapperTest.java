package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenceResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.*;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.HEARING_ID;


class CourtCaseListResponseMapperTest {

    private static final long ID = 1234L;
    private static final String CASE_ID = "CASE_ID";
    private static final String CASE_NO = "CASE_NO";
    private static final String URN = "URN001";
    private static final String COURT_CODE = "COURT_CODE";
    private static final String COURT_ROOM = "Courtroom 02";
    private static final LocalDateTime CREATED = LocalDateTime.now();
    private static final LocalDate HEARING_DATE = LocalDate.of(2020, 2, 25);
    private static final LocalDateTime SESSION_START_TIME = LocalDateTime.of(HEARING_DATE, LocalTime.of(9, 0));
    private static final String OFFENCE_TITLE = "OFFENCE_TITLE";
    private static final String OFFENCE_SUMMARY = "OFFENCE_SUMMARY";
    private static final String ACT = "ACT";
    private static final Integer OFFENCE_LIST_NO = 25;
    private static final String LIST_NO = "LIST_NO";
    private static final LocalDate DEFENDANT_DOB = LocalDate.of(1958, 2, 26);
    private static final CourtSession SESSION = CourtSession.MORNING;
    private static final LocalDateTime FIRST_CREATED = LocalDateTime.of(2020, 1, 1, 1, 1);

    private HearingDTO hearingDTO;

    @BeforeEach
    void setUp() {

        var hearings = Arrays.asList(
                HearingDayDTO.builder()
                        .day(HEARING_DATE)
                        .time(SESSION_START_TIME.toLocalTime())
                        .courtRoom(COURT_ROOM)
                        .courtCode(COURT_CODE)
                        .build(),
                HearingDayDTO.builder()
                        .day(HEARING_DATE.plusDays(1))
                        .time(SESSION_START_TIME.toLocalTime().plusHours(4))
                        .courtRoom("Courtroom 02")
                        .courtCode(COURT_CODE)
                        .build()
        );

        hearingDTO = buildCourtCaseEntity(hearings);
    }

    @Test
    void givenSeparateDefendant_whenMap_thenReturnMultipleResponses() {
        // Build defendant with deliberately different values from the defaults
        var defendantOffence = OffenceDTO.builder()
                .act(ACT)
                .sequence(1)
                .summary(OFFENCE_SUMMARY)
                .title(OFFENCE_TITLE)
                .listNo(OFFENCE_LIST_NO)
                .offenceCode("R8881")
                .plea(PleaEntity.builder().value("value").date(LocalDate.now()).build())
                .verdict(VerdictEntity.builder().typeDescription("description").date(LocalDate.now()).build())
                .build();
        var defendantUuid = UUID.randomUUID().toString();
        var defendantName = NamePropertiesEntity.builder().title("DJ").forename1("Giles").surname("PETERSON").build();
        var defendantDTO = HearingDefendantDTO.builder()
                .defendant(DefendantDTO.builder()
                        .crn("CRN123")
                        .defendantName(defendantName.getFullName())
                        .name(defendantName)
                        .address(AddressPropertiesEntity.builder().postcode("WN8 0PZ").build())
                        .sex(Sex.FEMALE)
                        .nationality1("Romanian")
                        .dateOfBirth(DEFENDANT_DOB.plusDays(2))
                        .offender(OffenderDTO.builder()
                                .crn("CRN123")
                                .preSentenceActivity(true)
                                .awaitingPsr(true)
                                .suspendedSentenceOrder(true)
                                .breach(true)
                                .probationStatus(OffenderProbationStatus.CURRENT)
                                .previouslyKnownTerminationDate(LocalDate.now())
                                .build())
                        .pnc("PNC123")
                        .cro("CRO123")
                        .defendantId(defendantUuid)
                        .type(DefendantType.PERSON)
                        .nationality1("Romanian")
                        .offenderConfirmed(true)
                        .build())
                .offences(singletonList(defendantOffence))
                .prepStatus("IN_PROGRESS")
                .build();

        var courtCaseResponse = CourtCaseListResponseMapper.mapFrom(hearingDTO, defendantDTO, 3, HEARING_DATE);

        assertCaseFields(courtCaseResponse, null, SourceType.COMMON_PLATFORM);
        assertHearingFields(courtCaseResponse);
        assertThat(courtCaseResponse.getOffences()).hasSize(1);
        assertOffenceFields(courtCaseResponse.getOffences().get(0));

        assertThat(courtCaseResponse.getDefendantId()).isEqualTo(defendantUuid);
        assertThat(courtCaseResponse.getDefendantAddress().getPostcode()).isEqualTo("WN8 0PZ");
        assertThat(courtCaseResponse.getDefendantDob()).isEqualTo(DEFENDANT_DOB.plusDays(2));
        assertThat(courtCaseResponse.getDefendantSex()).isEqualTo("F");
        assertThat(courtCaseResponse.getDefendantName()).isEqualTo("DJ Giles PETERSON");
        assertThat(courtCaseResponse.getDefendantType()).isSameAs(DefendantType.PERSON);
        assertThat(courtCaseResponse.getName().getTitle()).isEqualTo("DJ");
        assertThat(courtCaseResponse.getName().getForename1()).isEqualTo("Giles");
        assertThat(courtCaseResponse.getName().getSurname()).isEqualTo("PETERSON");
        assertThat(courtCaseResponse.getCrn()).isEqualTo("CRN123");
        assertThat(courtCaseResponse.getPnc()).isEqualTo("PNC123");
        assertThat(courtCaseResponse.getCro()).isEqualTo("CRO123");
        assertThat(courtCaseResponse.getNationality1()).isEqualTo("Romanian");
        assertThat(courtCaseResponse.getNationality2()).isNull();
        assertThat(courtCaseResponse.getAwaitingPsr()).isTrue();
        assertThat(courtCaseResponse.getPreSentenceActivity()).isTrue();
        assertThat(courtCaseResponse.getBreach()).isTrue();
        assertThat(courtCaseResponse.getSuspendedSentenceOrder()).isTrue();
        assertThat(courtCaseResponse.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.now());
        assertThat(courtCaseResponse.getProbationStatus().toUpperCase()).isEqualTo("CURRENT");
        assertThat(courtCaseResponse.getProbationStatusActual()).isEqualTo("CURRENT");
        assertThat(courtCaseResponse.getHearingType()).isEqualTo("sentence");

        assertThat(courtCaseResponse.getNumberOfPossibleMatches()).isEqualTo(3);
        assertThat(courtCaseResponse.getConfirmedOffender()).isTrue();
        assertThat(courtCaseResponse.getOffences()).hasSize(1);
        assertThat(courtCaseResponse.getOffences().get(0).getPlea().getPleaValue()).isEqualTo("value");
        assertThat(courtCaseResponse.getOffences().get(0).getVerdict().getVerdictType().getDescription()).isEqualTo("description");
        assertThat(courtCaseResponse.getCaseMarkers()).hasSize(1);
        assertThat(courtCaseResponse.getCaseMarkers().get(0).getMarkerTypeDescription()).isEqualTo("description");
    }

    private void assertOffenceFields(OffenceResponse offenceResponse) {
        assertThat(offenceResponse.getOffenceTitle()).isEqualTo(OFFENCE_TITLE);
        assertThat(offenceResponse.getOffenceSummary()).isEqualTo(OFFENCE_SUMMARY);
        assertThat(offenceResponse.getAct()).isEqualTo(ACT);
        assertThat(offenceResponse.getOffenceSummary()).isEqualTo(OFFENCE_SUMMARY);
        assertThat(offenceResponse.getListNo()).isEqualTo(OFFENCE_LIST_NO);
    }

    private void assertHearingFields(CourtCaseResponse courtCaseResponse) {
        assertThat(courtCaseResponse.getHearingId()).isEqualTo(HEARING_ID);
        assertThat(courtCaseResponse.getHearingEventType()).isEqualTo(HearingEventType.RESULTED);
        assertThat(courtCaseResponse.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(courtCaseResponse.getCourtRoom()).isEqualTo("2");
        assertThat(courtCaseResponse.getListNo()).isEqualTo(LIST_NO);
        assertThat(courtCaseResponse.getSession()).isEqualTo(SESSION);
        assertThat(courtCaseResponse.getSessionStartTime()).isEqualTo(SESSION_START_TIME);
        assertThat(courtCaseResponse.getHearings()).isNull();
    }

    private void assertCaseFields(CourtCaseResponse courtCaseResponse, String caseNo, SourceType sourceType) {
        Optional.ofNullable(caseNo)
                .ifPresentOrElse((c) -> assertThat(courtCaseResponse.getCaseNo()).isEqualTo(c), () -> assertThat(courtCaseResponse.getCaseNo()).isNull());
        assertThat(courtCaseResponse.getCaseId()).isEqualTo(CASE_ID);
        assertThat(courtCaseResponse.getSource()).isEqualTo(sourceType.name());
        assertThat(courtCaseResponse.isCreatedToday()).isFalse();
        assertThat(courtCaseResponse.getUrn()).isEqualTo(URN);
    }

    private HearingDTO buildCourtCaseEntity(List<HearingDayDTO> hearings) {

        HearingDTO.HearingDTOBuilder<?, ?> builder = HearingDTO.builder();

        return builder
                .id(ID)
                .hearingId(HEARING_ID)
                .hearingType("sentence")
                .listNo(LIST_NO)
                .hearingEventType(HearingEventType.RESULTED)
                .courtCase(CourtCaseDTO.builder()
                        .sourceType(SourceType.COMMON_PLATFORM)
                        .caseNo(CASE_NO)
                        .caseId(CASE_ID)
                        .created(CREATED)
                        .urn(URN)
                        .caseMarkers(List.of(CaseMarkerDTO.builder()
                                .typeDescription("description")
                                .build()))
                        .build())
                .firstCreated(CourtCaseListResponseMapperTest.FIRST_CREATED)
                .hearingDays(hearings)
                .build();
    }
}
