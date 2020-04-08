package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.*;
import uk.gov.justice.probation.courtcaseservice.service.model.*;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OffenderMapper {
    public Offender offenderFrom(CommunityApiOffenderResponse offenderResponse) {
        return Offender.builder()
                .crn(offenderResponse.getOtherIds().getCrn())
                .offenderManagers(
                        offenderResponse.getOffenderManagers().stream()
                                .map(this::buildOffenderManager)
                                .collect(Collectors.toList())
                )
                .build();
    }

    private OffenderManager buildOffenderManager(CommunityApiOffenderManager offenderManager) {
        var staff = offenderManager.getStaff();
        return OffenderManager.builder()
            .forenames(staff.getForenames())
            .surname(staff.getSurname())
            .allocatedDate(offenderManager.getFromDate())
            .build();
    }

    public List<Conviction> convictionsFrom(CommunityApiConvictionsResponse convictionsResponse) {
        return convictionsResponse.getConvictions().stream()
                .map(this::buildConviction)
                .collect(Collectors.toList());
    }

    private Conviction buildConviction(CommunityApiConvictionResponse conviction) {
        return Conviction.builder()
                .convictionId(conviction.getConvictionId())
                .active(conviction.getActive())
                .convictionDate(conviction.getConvictionDate())
                .offences(conviction.getOffences().stream()
                    .map(offence -> new Offence(offence.getDetail().getDescription()))
                    .collect(Collectors.toList())
                )
                .sentence(Optional.ofNullable(conviction.getSentence()).map(this::buildSentence).orElse(null))
                .endDate(endDateCalculator.apply(conviction.getConvictionDate(), conviction.getSentence()))
                .build();
    }

    public List<Requirement> requirementsFrom(CommunityApiRequirementsResponse requirementsResponse) {
        return requirementsResponse.getRequirements().stream()
                .map(this::buildRequirement)
                .collect(Collectors.toList());
    }

    private Requirement buildRequirement(CommunityApiRequirementResponse requirement) {
        return Requirement.builder()
                .requirementId(requirement.getRequirementId())
                .commencementDate(requirement.getCommencementDate())
                .startDate(requirement.getStartDate())
                .terminationDate(requirement.getTerminationDate())
                .expectedStartDate(requirement.getExpectedStartDate())
                .expectedEndDate(requirement.getExpectedEndDate())
                .active(requirement.getActive() != null && requirement.getActive().booleanValue())
                .length(requirement.getLength())
                .lengthUnit(requirement.getLengthUnit())
                .adRequirementTypeMainCategory(requirement.getAdRequirementTypeMainCategory())
                .adRequirementTypeSubCategory(requirement.getAdRequirementTypeSubCategory())
                .requirementTypeMainCategory(requirement.getRequirementTypeMainCategory())
                .requirementTypeSubCategory(requirement.getRequirementTypeSubCategory())
                .terminationReason(requirement.getTerminationReason())
                .build();
    }

    private Sentence buildSentence(final CommunityApiSentence communityApiSentence) {
        return Sentence.builder()
            .description(communityApiSentence.getDescription())
            .length(communityApiSentence.getOriginalLength())
            .lengthUnits(communityApiSentence.getOriginalLengthUnits())
            .lengthInDays(communityApiSentence.getLengthInDays())
            .terminationDate(communityApiSentence.getTerminationDate())
            .terminationReason(communityApiSentence.getTerminationReason())
            .unpaidWork(Optional.ofNullable(communityApiSentence.getUnpaidWork()).map(this::buildUnpaidWork).orElse(null))
            .build();
    }

    private UnpaidWork buildUnpaidWork(final CommunityApiUnpaidWork communityApiUnpaidWork) {
        return UnpaidWork.builder()
                .minutesOffered(communityApiUnpaidWork.getMinutesOrdered())
                .minutesCompleted(communityApiUnpaidWork.getMinutesCompleted())
                .appointmentsToDate(communityApiUnpaidWork.getAppointments().getTotal())
                .attended(communityApiUnpaidWork.getAppointments().getAttended())
                .acceptableAbsences(communityApiUnpaidWork.getAppointments().getAcceptableAbsences())
                .unacceptableAbsences(communityApiUnpaidWork.getAppointments().getUnacceptableAbsences())
                .build();
    }

    final BiFunction<LocalDate, CommunityApiSentence, LocalDate> endDateCalculator = (convictionDate, sentence) -> {

        if (convictionDate == null || sentence == null) {
            return null;
        }

        return convictionDate.plus(sentence.getLengthInDays(), ChronoUnit.DAYS);
    };
}
