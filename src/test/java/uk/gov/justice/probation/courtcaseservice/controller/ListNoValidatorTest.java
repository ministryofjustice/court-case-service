package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.controller.model.Defendant;
import uk.gov.justice.probation.courtcaseservice.controller.model.ExtendedHearingRequestResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingDay;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenceRequestResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;

import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListNoValidatorTest {

    @Mock
    ConstraintValidatorContext constraintValidatorContext;

    @Mock
    ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    private static final ListNoValidator LIST_NO_VALIDATOR = new ListNoValidator();

    @BeforeEach
    void setup() {
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);
        when(constraintViolationBuilder.addConstraintViolation()).thenReturn(constraintValidatorContext);
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(constraintValidatorContext, constraintViolationBuilder);
    }

    private static List<HearingDay> HEARING_DAYS_WITHOUT_LIST_NO = List.of(
            HearingDay.builder().sessionStartTime(LocalDateTime.now()).build(),
            HearingDay.builder().sessionStartTime(LocalDateTime.now()).build());

    private static List<OffenceRequestResponse> OFFENCES_WITHOUT_LIST_NO = List.of(OffenceRequestResponse.builder().build(),
            OffenceRequestResponse.builder().build());

    private static List<HearingDay> HEARING_DAYS_WITH_LIST_NO = List.of(
            HearingDay.builder().listNo("10").sessionStartTime(LocalDateTime.now()).build(),
            HearingDay.builder().listNo("20").sessionStartTime(LocalDateTime.now()).build());

    private static List<OffenceRequestResponse> OFFENCES_WITH_LIST_NO = List.of(
            OffenceRequestResponse.builder().listNo(30).build(),
            OffenceRequestResponse.builder().listNo(40).build()
    );

    @Test
    void whenListNoInNeitherOfHearingDaysAndDefendantOffences_thenFailValidation() {

        var courtCase = ExtendedHearingRequestResponse.builder()
                .hearingDays(HEARING_DAYS_WITHOUT_LIST_NO).defendants(
                        List.of(Defendant.builder().offences(OFFENCES_WITHOUT_LIST_NO).build(),
                        Defendant.builder().offences(OFFENCES_WITHOUT_LIST_NO).build())).build();

        assertThat(LIST_NO_VALIDATOR.isValid(courtCase, constraintValidatorContext)).isFalse();

        Mockito.verify(constraintValidatorContext).buildConstraintViolationWithTemplate("listNo should be provided in either hearingDays[] or defendants[].offences[]");
        Mockito.verify(constraintViolationBuilder).addConstraintViolation();
    }

    @Test
    void whenListNoIsInBothHearingDaysAndDefendantOffences_thenFailValidation() {

        var courtCase = ExtendedHearingRequestResponse.builder()
                .hearingDays(HEARING_DAYS_WITH_LIST_NO).defendants(
                        List.of(Defendant.builder().offences(OFFENCES_WITH_LIST_NO).build())).build();

        assertThat(LIST_NO_VALIDATOR.isValid(courtCase, constraintValidatorContext)).isFalse();
        Mockito.verify(constraintValidatorContext).buildConstraintViolationWithTemplate("Only one of hearingDays[].listNo and defendants[].offences[].listNo must be provided");
        Mockito.verify(constraintViolationBuilder).addConstraintViolation();
    }

    @Test
    void whenListNoIsNotProvidedInAllOfTheHearingDays_thenFailValidation() {

        var hearingDays = new ArrayList<HearingDay>(List.of(HearingDay.builder().build()));
        hearingDays.addAll(HEARING_DAYS_WITH_LIST_NO);

        var courtCase = ExtendedHearingRequestResponse.builder()
                .hearingDays(hearingDays).defendants(List.of(Defendant.builder().offences(OFFENCES_WITHOUT_LIST_NO).build())).build();

        assertThat(LIST_NO_VALIDATOR.isValid(courtCase, constraintValidatorContext)).isFalse();
        Mockito.verify(constraintValidatorContext).buildConstraintViolationWithTemplate("listNo is missing from one or more hearingDays[]");
        Mockito.verify(constraintViolationBuilder).addConstraintViolation();
    }

    @Test
    void whenListNoIsNotProvidedInAllOfTheOffences_thenFailValidation() {

        var hearingDays = List.of(
                HearingDay.builder().build(),
                HearingDay.builder().build());

        var courtCase = ExtendedHearingRequestResponse.builder()
                .hearingDays(hearingDays).defendants(
                        List.of(Defendant.builder().offences(OFFENCES_WITH_LIST_NO).build(),
                                Defendant.builder().offences(OFFENCES_WITHOUT_LIST_NO).build())
                ).build();

        assertThat(LIST_NO_VALIDATOR.isValid(courtCase, constraintValidatorContext)).isFalse();
        Mockito.verify(constraintValidatorContext).buildConstraintViolationWithTemplate("listNo missing in one or more defendants[].offences[]");
        Mockito.verify(constraintViolationBuilder).addConstraintViolation();
    }

    @Test
    void givenPutWith_NoListNoInHearingDays_WithListNoInDefendantOffences_Accepted() {

        var courtCase = ExtendedHearingRequestResponse.builder()
                .hearingDays(HEARING_DAYS_WITHOUT_LIST_NO).defendants(
                        List.of(Defendant.builder().name(NamePropertiesEntity.builder().forename1("Foreone").surname("Surone").build())
                                        .offences(OFFENCES_WITH_LIST_NO).build(),
                                Defendant.builder().name(NamePropertiesEntity.builder().forename1("Foretwo").surname("Surtwo").build())
                                        .offences(OFFENCES_WITH_LIST_NO).build())
                ).build();
        assertThat(LIST_NO_VALIDATOR.isValid(courtCase, constraintValidatorContext)).isTrue();
        verifyNoInteractions(constraintValidatorContext);
        verifyNoInteractions(constraintViolationBuilder);
    }

    @Test
    void whenNoListNoInDefendantOffences_WithListNoInHearingDays_Accepted() {

        var courtCase = ExtendedHearingRequestResponse.builder()
                .hearingDays(HEARING_DAYS_WITH_LIST_NO).defendants(
                        List.of(Defendant.builder().name(NamePropertiesEntity.builder().forename1("Foreone").surname("Surone").build())
                                        .offences(OFFENCES_WITHOUT_LIST_NO).build(),
                                Defendant.builder().name(NamePropertiesEntity.builder().forename1("Foretwo").surname("Surtwo").build())
                                        .offences(OFFENCES_WITHOUT_LIST_NO).build())
                ).build();

        assertThat(LIST_NO_VALIDATOR.isValid(courtCase, constraintValidatorContext)).isTrue();
        verifyNoInteractions(constraintValidatorContext);
        verifyNoInteractions(constraintViolationBuilder);
    }
}