package uk.gov.justice.probation.courtcaseservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.justice.probation.courtcaseservice.controller.model.ExtendedHearingRequestResponse;
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
 * Custom constraint validator on @{@link ExtendedHearingRequestResponse} to cross validate listNo from hearingDays
 * with that of listNo from defendants[].offences[].
 * request with no hearingDays.listNo with defendants[].offence[].listNo is accepted
 * request with hearingDays.listNo and no defendants[].offence[].listNo is accepted
 * request with neither is rejected
 * request with both is rejected
 */
@Slf4j
public class ListNoValidator implements ConstraintValidator<ValidateListNo, ExtendedHearingRequestResponse> {

    @Override
    public void initialize(ValidateListNo constraintAnnotation) {
        log.info("Initializing ListNoValidator");
    }

    @Override
    public boolean isValid(ExtendedHearingRequestResponse extendedHearingRequestResponse,
                           ConstraintValidatorContext context) {
        var validationError = validateListNo(extendedHearingRequestResponse);
        return validationError.map(message -> {
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            return false;
        }).orElse(true);
    }

    private List<OffenceRequestResponse> getAllOffences(ExtendedHearingRequestResponse courtCase) {
        return Optional.ofNullable(courtCase.getDefendants())
                .orElse(Collections.emptyList())
                .stream().map(defendant -> Optional.ofNullable(defendant.getOffences()).orElse(Collections.emptyList()))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    private Optional<String> validateListNo(ExtendedHearingRequestResponse courtCase) {

        List<HearingDay> hearingDays = Optional.ofNullable(courtCase.getHearingDays())
                .orElse(Collections.emptyList());

        var hearingDaysWitListNo = hearingDays.stream().filter(hearingDay -> StringUtils.isNotBlank(hearingDay.getCourtCode())).count();

        if (hearingDaysWitListNo > 0) {
            if (hearingDaysWitListNo != hearingDays.size()) {
                return Optional.of("listNo is missing from one or more hearingDays[]");
            }

            var offencesHasListNo = getAllOffences(courtCase).stream()
                    .anyMatch(offenceRequestResponse -> Objects.nonNull(offenceRequestResponse.getListNo()));

            if (offencesHasListNo) {
                return Optional.of("Only one of hearingDays[].listNo and defendants[].offences[].listNo must be provided");
            }
        } else {
            List<OffenceRequestResponse> allOffences = getAllOffences(courtCase);
            var offencesWithListNo = allOffences.stream()
                    .filter(offenceRequestResponse -> Objects.nonNull(offenceRequestResponse.getListNo())).collect(Collectors.toList());

            if (offencesWithListNo.isEmpty()) {
                return Optional.of("listNo should be provided in either hearingDays[] or defendants[].offences[]");
            }
            if (allOffences.stream().anyMatch(offenceRequestResponses -> Objects.isNull(offenceRequestResponses.getListNo()))) {
                return Optional.of("listNo missing in one or more defendants[].offences[]");
            }
        }

        return Optional.ofNullable(null);
    }
}