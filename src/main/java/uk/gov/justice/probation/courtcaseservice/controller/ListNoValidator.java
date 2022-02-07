package uk.gov.justice.probation.courtcaseservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.controller.model.ExtendedCourtCaseRequestResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingDay;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenceRequestResponse;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Custom constraint validator on @{@link ExtendedCourtCaseRequestResponse} to cross validate listNo from hearingDays
 * with that of listNo from defendants[].offences[].
 * request with no hearingDays.listNo with defendants[].offence[].listNo is accepted
 * request with hearingDays.listNo and no defendants[].offence[].listNo is accepted
 * request with neither is rejected
 * request with both is rejected
 */
@Slf4j
public class ListNoValidator implements ConstraintValidator<ValidateListNo, ExtendedCourtCaseRequestResponse> { // 1.

    @Override
    public void initialize(ValidateListNo constraintAnnotation) {
        log.info("Initializing ListNoValidator");
    }

    @Override
    public boolean isValid(ExtendedCourtCaseRequestResponse extendedCourtCaseRequestResponse,
                           ConstraintValidatorContext context) {
        validateListNo(extendedCourtCaseRequestResponse);
        return true;
    }

    private List<OffenceRequestResponse> getAllOffences(ExtendedCourtCaseRequestResponse courtCase) {
        return Optional.ofNullable(courtCase.getDefendants())
                .orElse(Collections.emptyList())
                .stream().map(defendant -> Optional.ofNullable(defendant.getOffences()).orElse(Collections.emptyList()))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    private void validateListNo(ExtendedCourtCaseRequestResponse courtCase) {

        List<HearingDay> hearingDays = Optional.ofNullable(courtCase.getHearingDays())
                .orElse(Collections.emptyList());

        var hearingDaysWitListNo = hearingDays.stream().filter(hearingDay -> StringUtils.isNotBlank(hearingDay.getListNo())).count();

        if(hearingDaysWitListNo > 0) {
            if(hearingDaysWitListNo != hearingDays.size()) {
                throw new ConflictingInputException("listNo is missing from one or more hearingDays[]");
            }

            var offencesHasListNo = getAllOffences(courtCase).stream()
                    .anyMatch(offenceRequestResponse -> Objects.nonNull(offenceRequestResponse.getListNo()));

            if(offencesHasListNo) {
                throw new ConflictingInputException("Only one of hearingDays[].listNo and defendants[].offences[].listNo must be provided");
            }
        } else {
            List<OffenceRequestResponse> allOffences = getAllOffences(courtCase);
            var offencesWithListNo = allOffences.stream()
                    .filter(offenceRequestResponse -> Objects.nonNull(offenceRequestResponse.getListNo())).collect(Collectors.toList());

            if(offencesWithListNo.isEmpty()) {
                throw new ConflictingInputException("listNo should be provided in either hearingDays[] or defendants[].offences[]");
            }
            if (allOffences.stream().anyMatch(offenceRequestResponses -> Objects.isNull(offenceRequestResponses.getListNo()))) {
                throw new ConflictingInputException("listNo missing in one or more defendants[].offences[]");
            }
        }
    }
}