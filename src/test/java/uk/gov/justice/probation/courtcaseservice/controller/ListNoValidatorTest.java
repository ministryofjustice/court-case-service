package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.controller.model.Defendant;
import uk.gov.justice.probation.courtcaseservice.controller.model.ExtendedCourtCaseRequestResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingDay;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenceRequestResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;

import java.time.LocalDateTime;
import java.util.List;

class ListNoValidatorTest {

    private static final ListNoValidator LIST_NO_VALIDATOR = new ListNoValidator();

    @Test()
    void whenListNoInNeitherOfHearingDaysAndDefendantOffences_thenThrow() {

        var hearingDays = List.of(
                HearingDay.builder().sessionStartTime(LocalDateTime.now()).build(),
                HearingDay.builder().sessionStartTime(LocalDateTime.now()).build());
        var offences = List.of(OffenceRequestResponse.builder().build());

        var courtCase = ExtendedCourtCaseRequestResponse.builder()
                .hearingDays(hearingDays).defendants(List.of(Defendant.builder().offences(offences).build(),
                        Defendant.builder().offences(offences).build())).build();
        ConflictingInputException conflictingInputException = Assertions.assertThrows(ConflictingInputException.class,
                () -> LIST_NO_VALIDATOR.isValid(courtCase, null), "Exception must be thrown");
        Assertions.assertEquals(conflictingInputException.getMessage(),
                "listNo should be provided in either hearingDays[] or defendants[].offences[]");
    }

    @Test()
    void whenListNoIsInBothHearingDaysAndDefendantOffences_thenThrow() {

        var hearingDays = List.of(
                HearingDay.builder().listNo("list1").build(),
                HearingDay.builder().listNo("list2").build());
        var offences = List.of(OffenceRequestResponse.builder().listNo(20).build());

        var courtCase = ExtendedCourtCaseRequestResponse.builder()
                .hearingDays(hearingDays).defendants(List.of(Defendant.builder().offences(offences).build())).build();

        ConflictingInputException conflictingInputException = Assertions.assertThrows(ConflictingInputException.class,
                () -> LIST_NO_VALIDATOR.isValid(courtCase, null), "Exception must be thrown");
        Assertions.assertEquals(conflictingInputException.getMessage(),
                "Only one of hearingDays[].listNo and defendants[].offences[].listNo must be provided");
    }

    @Test()
    void whenListNoIsNotProvidedInAllOfTheHearingDays_thenThrow() {

        var hearingDays = List.of(
                HearingDay.builder().listNo("list1").build(),
                HearingDay.builder().build(),
                HearingDay.builder().listNo("list2").build());
        var offences = List.of(OffenceRequestResponse.builder().build());

        var courtCase = ExtendedCourtCaseRequestResponse.builder()
                .hearingDays(hearingDays).defendants(List.of(Defendant.builder().offences(offences).build())).build();

        ConflictingInputException conflictingInputException = Assertions.assertThrows(ConflictingInputException.class,
                () -> LIST_NO_VALIDATOR.isValid(courtCase, null), "Exception must be thrown");
        Assertions.assertEquals(conflictingInputException.getMessage(),
                "listNo is missing from one or more hearingDays[]");
    }

    @Test()
    void whenListNoIsNotProvidedInAllOfTheOffences_thenThrow() {

        var hearingDays = List.of(
                HearingDay.builder().build(),
                HearingDay.builder().build());
        var offences1 = List.of(
                OffenceRequestResponse.builder().listNo(10).build(),
                OffenceRequestResponse.builder().listNo(10).build()
        );
        var offences2 = List.of(
                OffenceRequestResponse.builder().listNo(30).build(),
                OffenceRequestResponse.builder().build()
        );

        var courtCase = ExtendedCourtCaseRequestResponse.builder()
                .hearingDays(hearingDays).defendants(
                        List.of(Defendant.builder().offences(offences1).build(),
                                Defendant.builder().offences(offences2).build())
                ).build();

        ConflictingInputException conflictingInputException = Assertions.assertThrows(ConflictingInputException.class,
                () -> LIST_NO_VALIDATOR.isValid(courtCase, null), "Exception must be thrown");
        Assertions.assertEquals(conflictingInputException.getMessage(),
                "listNo missing in one or more defendants[].offences[]");
    }

    @Test()
    void givenPutWith_NoListNoInHearingDays_WithListNoInDefendantOffences_Accepted() {

        var hearingDays = List.of(
                HearingDay.builder().sessionStartTime(LocalDateTime.now()).build(),
                HearingDay.builder().sessionStartTime(LocalDateTime.now()).build());
        var offences1 = List.of(
                OffenceRequestResponse.builder().listNo(10).build(),
                OffenceRequestResponse.builder().listNo(20).build()
        );
        var offences2 = List.of(
                OffenceRequestResponse.builder().listNo(30).build()
        );

        var courtCase = ExtendedCourtCaseRequestResponse.builder()
                .hearingDays(hearingDays).defendants(
                        List.of(Defendant.builder().name(NamePropertiesEntity.builder().forename1("Foreone").surname("Surone").build()).offences(offences1).build(),
                                Defendant.builder().name(NamePropertiesEntity.builder().forename1("Foretwo").surname("Surtwo").build()).offences(offences2).build())
                ).build();

        Assertions.assertTrue(LIST_NO_VALIDATOR.isValid(courtCase, null), "Should return true");
    }

    @Test()
    void givenPutWith_NoListNoInDefendantOffences_WithListNoInHearingDays_Accepted() {

        var hearingDays = List.of(
                HearingDay.builder().listNo("10").sessionStartTime(LocalDateTime.now()).build(),
                HearingDay.builder().listNo("20").sessionStartTime(LocalDateTime.now()).build());
        var offences1 = List.of(
                OffenceRequestResponse.builder().build(),
                OffenceRequestResponse.builder().build()
        );
        var offences2 = List.of(
                OffenceRequestResponse.builder().build()
        );

        var courtCase = ExtendedCourtCaseRequestResponse.builder()
                .hearingDays(hearingDays).defendants(
                        List.of(Defendant.builder().name(NamePropertiesEntity.builder().forename1("Foreone").surname("Surone").build()).offences(offences1).build(),
                                Defendant.builder().name(NamePropertiesEntity.builder().forename1("Foretwo").surname("Surtwo").build()).offences(offences2).build())
                ).build();

        Assertions.assertTrue(LIST_NO_VALIDATOR.isValid(courtCase, null), "Should return true");
    }


}