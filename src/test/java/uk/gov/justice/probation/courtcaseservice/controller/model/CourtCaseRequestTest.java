package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.AWAITING_PSR;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.BREACH;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRO;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_DOB;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_NAME;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_PHONE_NUMBER;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_PHONE_NUMBER;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_SEX;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.NAME;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.NATIONALITY_1;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.NATIONALITY_2;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.PNC;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.PRE_SENTENCE_ACTIVITY;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.PROBATION_STATUS;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.SUSPENDED_SENTENCE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.TERMINATION_DATE;

class CourtCaseRequestTest {

    private static final LocalDateTime SESSION_START_TIME = LocalDateTime.of(2020, 10, 5, 9, 30);

    @Test
    void givenAllFieldsPresent_whenCallAsEntity_returnHearingEntity() {
        final var request = CourtCaseRequest.builder()
                .caseId("CASE_ID")
                .caseNo("CASE_NO")
                .courtCode("COURT_CODE")
                .courtRoom("COURT_ROOM")
                .source("LIBRA")
                .sessionStartTime(SESSION_START_TIME)
                .probationStatus(PROBATION_STATUS)
                .previouslyKnownTerminationDate(TERMINATION_DATE)
                .suspendedSentenceOrder(SUSPENDED_SENTENCE)
                .breach(BREACH)
                .preSentenceActivity(PRE_SENTENCE_ACTIVITY)
                .offences(Arrays.asList(
                                new OffenceRequestResponse("OFFENCE_TITLE1", "OFFENCE_SUMMARY1", "ACT1", 10, Collections.emptyList()),
                                new OffenceRequestResponse("OFFENCE_TITLE2", "OFFENCE_SUMMARY2", "ACT2", 20, Collections.emptyList())
                        )
                )
                .defendantDob(DEFENDANT_DOB)
                .defendantId(DEFENDANT_ID)
                .name(NAME)
                .defendantName(NAME.getFullName())
                .defendantAddress(new AddressRequestResponse("LINE1", "LINE2", "LINE3", "LINE4", "LINE5", "POSTCODE"))
                .defendantSex(DEFENDANT_SEX)
                .defendantType(DefendantType.PERSON)
                .crn(CRN)
                .pnc(PNC)
                .cro(CRO)
                .listNo("LIST_NO")
                .nationality1(NATIONALITY_1)
                .nationality2(NATIONALITY_2)
                .awaitingPsr(AWAITING_PSR)
                .phoneNumber(DEFENDANT_PHONE_NUMBER)
                .build();

        final var entity = request.asEntity();

        assertThat(entity.getCaseId()).isEqualTo("CASE_ID");
        assertThat(entity.getCaseNo()).isEqualTo("CASE_NO");
        assertThat(entity.getSourceType()).isSameAs(SourceType.LIBRA);

        assertThat(entity.getHearingDays()).hasSize(1);
        assertThat(entity.getHearingDefendants()).hasSize(1);
        final var hearingDefendant = entity.getHearingDefendants().get(0);

        final var address = AddressPropertiesEntity.builder()
            .line1("LINE1")
            .line2("LINE2")
            .line3("LINE3")
            .line4("LINE4")
            .line5("LINE5")
            .postcode("POSTCODE")
            .build();
        final var expectedHearingDefendant = EntityHelper.aHearingDefendantEntity(address);
        assertThat(hearingDefendant)
            .usingRecursiveComparison()
            .ignoringFields("id", "hearing", "offences", "defendant.offender")
            .isEqualTo(expectedHearingDefendant);
        assertThat(hearingDefendant.getHearing()).isNotNull();

        assertThat(hearingDefendant.getOffences()).hasSize(2);
        var expectedDefendantOffenceEntity1 = OffenceEntity.builder()
            .summary("OFFENCE_SUMMARY1")
            .title("OFFENCE_TITLE1")
            .act("ACT1")
            .sequence(1)
            .listNo(10)
            .build();
        assertThat(hearingDefendant.getOffences().get(0))
            .usingRecursiveComparison()
            .ignoringFields("id", "hearingDefendant")
            .isEqualTo(expectedDefendantOffenceEntity1);
        assertThat(hearingDefendant.getOffences().get(0).getHearingDefendant()).isNotNull();

        final var offender = hearingDefendant.getDefendant().getOffender();
        assertThat(offender.getCrn()).isEqualTo(CRN);
        assertThat(offender.getProbationStatus()).isSameAs(OffenderProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(offender.getPreviouslyKnownTerminationDate()).isEqualTo(TERMINATION_DATE);
        assertThat(offender.getAwaitingPsr()).isEqualTo(AWAITING_PSR);
        assertThat(offender.isBreach()).isEqualTo(BREACH);
        assertThat(offender.isPreSentenceActivity()).isEqualTo(PRE_SENTENCE_ACTIVITY);
        assertThat(offender.isSuspendedSentenceOrder()).isEqualTo(SUSPENDED_SENTENCE);
    }

    @Test
    void givenOptionalFieldsNotPresent_whenCallAsEntity_returnCourtCaseEntity() {
        final var request = CourtCaseRequest.builder()
            .caseId("CASE_ID")
            .caseNo("CASE_NO")
            .courtCode("COURT_CODE")
            .courtRoom("COURT_ROOM")
            .sessionStartTime(SESSION_START_TIME)
            .probationStatus("PREVIOUSLY_KNOWN")
            .defendantName(DEFENDANT_NAME)
            .defendantDob(DEFENDANT_DOB)
            .build();

        final var entity = request.asEntity();

        assertThat(entity.getCaseId()).isEqualTo("CASE_ID");
        assertThat(entity.getCaseNo()).isEqualTo("CASE_NO");
        assertThat(entity.getSourceType()).isSameAs(SourceType.LIBRA);

        assertThat(entity.getHearingDays()).hasSize(1);
        assertThat(entity.getHearingDefendants()).hasSize(1);
        final var hearingDefendant = entity.getHearingDefendants().get(0);
        assertThat(hearingDefendant.getDefendantId()).isNotNull();
        assertThat(hearingDefendant.getDefendant().getDefendantId()).isEqualTo(hearingDefendant.getDefendantId());
        final var defendant = hearingDefendant.getDefendant();
        assertThat(defendant.getDefendantName()).isEqualTo(DEFENDANT_NAME);
        assertThat(defendant.getDateOfBirth()).isEqualTo(DEFENDANT_DOB);
        assertThat(defendant.getOffender()).isNull();
    }

    @Test
    void givenOptionalOffenderFieldsNotPresent_whenCallAsEntity_returnCourtCaseEntityWithOffenderDefaults() {
        final var request = CourtCaseRequest.builder()
            .caseId("CASE_ID")
            .caseNo("CASE_NO")
            .courtCode("COURT_CODE")
            .courtRoom("COURT_ROOM")
            .sessionStartTime(SESSION_START_TIME)
            .probationStatus("PREVIOUSLY_KNOWN")
            .defendantName(DEFENDANT_NAME)
            .defendantDob(DEFENDANT_DOB)
            .crn("CRN")
                .previouslyKnownTerminationDate(LocalDate.of(2021, 1, 1))
            .build();

        final var entity = request.asEntity();

        final var offender = entity.getHearingDefendants().get(0).getDefendant().getOffender();
        assertThat(offender).isNotNull();
        assertThat(offender.isBreach()).isFalse();
        assertThat(offender.isPreSentenceActivity()).isFalse();
        assertThat(offender.isSuspendedSentenceOrder()).isFalse();
    }
}
