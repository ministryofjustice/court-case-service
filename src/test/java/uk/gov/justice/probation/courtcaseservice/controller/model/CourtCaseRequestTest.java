package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantOffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRO;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_DOB;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_NAME;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_SEX;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.NAME;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.NATIONALITY_1;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.NATIONALITY_2;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.PNC;

class CourtCaseRequestTest {

    private static final LocalDateTime SESSION_START_TIME = LocalDateTime.of(2020, 10, 5, 9, 30);
    private static final LocalDate PREVIOUS_TERMINATION_DATE = LocalDate.of(2020, 9, 5);
    private static final boolean SUSPENDED_SENTENCE_ORDER = true;
    private static final boolean BREACH = false;
    private static final boolean PRE_SENTENCE_ACTIVITY = true;

    @Test
    void givenAllFieldsPresent_whenCallAsEntity_returnCourtCaseEntity() {
        final var request = CourtCaseRequest.builder()
                                                            .caseId("CASE_ID")
                                                            .caseNo("CASE_NO")
                                                            .courtCode("COURT_CODE")
                                                            .courtRoom("COURT_ROOM")
                                                            .sessionStartTime(SESSION_START_TIME)
                                                            .probationStatus("PROBATION_STATUS")
                                                            .previouslyKnownTerminationDate(PREVIOUS_TERMINATION_DATE)
                                                            .suspendedSentenceOrder(SUSPENDED_SENTENCE_ORDER)
                                                            .breach(BREACH)
                                                            .preSentenceActivity(PRE_SENTENCE_ACTIVITY)
                                                            .offences(Arrays.asList(
                                                                    new OffenceRequest("OFFENCE_TITLE1", "OFFENCE_SUMMARY1","ACT1"),
                                                                    new OffenceRequest("OFFENCE_TITLE2", "OFFENCE_SUMMARY2","ACT2")
                                                                )
                                                            )
                                                            .defendantDob(DEFENDANT_DOB)
                                                            .name(NAME)
                                                            .defendantName(NAME.getFullName())
                                                            .defendantAddress(new AddressRequest("LINE1", "LINE2", "LINE3", "LINE4", "LINE5", "POSTCODE"))
                                                            .defendantSex(DEFENDANT_SEX)
                                                            .defendantType(DefendantType.PERSON)
                                                            .crn(CRN)
                                                            .pnc(PNC)
                                                            .cro(CRO)
                                                            .listNo("LIST_NO")
                                                            .nationality1(NATIONALITY_1)
                                                            .nationality2(NATIONALITY_2)
                                                            .awaitingPsr(false)
                                                            .build();

        final var entity = request.asEntity();

        assertThat(entity.getCaseId()).isEqualTo("CASE_ID");
        assertThat(entity.getCaseNo()).isEqualTo("CASE_NO");
        assertThat(entity.getCourtCode()).isEqualTo("COURT_CODE");
        assertThat(entity.getCourtRoom()).isEqualTo("COURT_ROOM");
        assertThat(entity.getSessionStartTime()).isEqualTo(SESSION_START_TIME);
        assertThat(entity.getProbationStatus()).isEqualTo("PROBATION_STATUS");
        assertThat(entity.getPreviouslyKnownTerminationDate()).isEqualTo(PREVIOUS_TERMINATION_DATE);
        assertThat(entity.getSuspendedSentenceOrder()).isEqualTo(SUSPENDED_SENTENCE_ORDER);
        assertThat(entity.getBreach()).isEqualTo(BREACH);
        assertThat(entity.getPreSentenceActivity()).isEqualTo(PRE_SENTENCE_ACTIVITY);
        assertThat(entity.getName()).isEqualTo(NAME);
        assertThat(entity.getDefendantName()).isEqualTo(NAME.getFullName());
        assertThat(entity.getDefendantAddress().getLine1()).isEqualTo("LINE1");
        assertThat(entity.getDefendantAddress().getLine2()).isEqualTo("LINE2");
        assertThat(entity.getDefendantAddress().getLine3()).isEqualTo("LINE3");
        assertThat(entity.getDefendantAddress().getLine4()).isEqualTo("LINE4");
        assertThat(entity.getDefendantAddress().getLine5()).isEqualTo("LINE5");
        assertThat(entity.getDefendantAddress().getPostcode()).isEqualTo("POSTCODE");
        assertThat(entity.getDefendantDob()).isEqualTo(DEFENDANT_DOB);
        assertThat(entity.getDefendantSex()).isEqualTo(DEFENDANT_SEX);
        assertThat(entity.getCrn()).isEqualTo(CRN);
        assertThat(entity.getPnc()).isEqualTo(PNC);
        assertThat(entity.getCro()).isEqualTo(CRO);
        assertThat(entity.getListNo()).isEqualTo("LIST_NO");
        assertThat(entity.getNationality1()).isEqualTo(NATIONALITY_1);
        assertThat(entity.getNationality2()).isEqualTo(NATIONALITY_2);
        assertThat(entity.getAwaitingPsr()).isEqualTo(false);
        var expectedOffenceEntity1 = OffenceEntity.builder()
                .offenceSummary("OFFENCE_SUMMARY1")
                .offenceTitle("OFFENCE_TITLE1")
                .act("ACT1")
                .sequenceNumber(1)
                .build();
        var expectedOffenceEntity2 = OffenceEntity.builder()
            .offenceSummary("OFFENCE_SUMMARY2")
            .offenceTitle("OFFENCE_TITLE2")
            .act("ACT2")
            .sequenceNumber(2)
            .build();
        assertThat(entity.getOffences().get(0)).usingRecursiveComparison()
            .ignoringFields("courtCase", "id")
            .isEqualTo(expectedOffenceEntity1);
        assertThat(entity.getOffences().get(1)).usingRecursiveComparison()
            .ignoringFields("courtCase", "id")
            .isEqualTo(expectedOffenceEntity2);

        assertThat(entity.getHearings()).hasSize(1);
        assertThat(entity.getDefendants()).hasSize(1);

        final var address = AddressPropertiesEntity.builder()
            .line1("LINE1")
            .line2("LINE2")
            .line3("LINE3")
            .line4("LINE4")
            .line5("LINE5")
            .postcode("POSTCODE")
            .build();
        final var expectedDefendant = EntityHelper.aDefendantEntity(address);
        assertThat(entity.getDefendants().get(0))
            .usingRecursiveComparison()
            .ignoringFields("id", "courtCase", "offences")
            .isEqualTo(expectedDefendant);
        assertThat(entity.getDefendants().get(0).getCourtCase()).isNotNull();

        assertThat(entity.getDefendants().get(0).getOffences()).hasSize(2);
        var expectedDefendantOffenceEntity1 = DefendantOffenceEntity.builder()
            .summary("OFFENCE_SUMMARY1")
            .title("OFFENCE_TITLE1")
            .act("ACT1")
            .sequence(1)
            .build();
        assertThat(entity.getDefendants().get(0).getOffences().get(0))
            .usingRecursiveComparison()
            .ignoringFields("id", "defendant")
            .isEqualTo(expectedDefendantOffenceEntity1);
        assertThat(entity.getDefendants().get(0).getOffences().get(0).getDefendant()).isNotNull();
    }

    @Test
    void givenOptionalFieldsNotPresent_whenCallAsEntity_returnCourtCaseEntity() {
        final var request = CourtCaseRequest.builder()
            .caseId("CASE_ID")
            .caseNo("CASE_NO")
            .courtCode("COURT_CODE")
            .courtRoom("COURT_ROOM")
            .sessionStartTime(SESSION_START_TIME)
            .probationStatus("PROBATION_STATUS")
            .defendantName(DEFENDANT_NAME)
            .defendantDob(DEFENDANT_DOB)
            .build();

        final var entity = request.asEntity();

        assertThat(entity.getCaseId()).isEqualTo("CASE_ID");
        assertThat(entity.getCaseNo()).isEqualTo("CASE_NO");
        assertThat(entity.getCourtCode()).isEqualTo("COURT_CODE");
        assertThat(entity.getCourtRoom()).isEqualTo("COURT_ROOM");
        assertThat(entity.getSessionStartTime()).isEqualTo(SESSION_START_TIME);
        assertThat(entity.getProbationStatus()).isEqualTo("PROBATION_STATUS");

        assertThat(entity.getHearings()).hasSize(1);
    }
}
