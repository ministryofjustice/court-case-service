package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.controller.model.CurrentOrderHeaderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCurrentOrderHeaderDetailResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderManager;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRequirementResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiSentence;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiUnpaidWork;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Offence;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderManager;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;


@Component
public class OffenderMapper {
    public ProbationRecord probationRecordFrom(CommunityApiOffenderResponse offenderResponse) {
        return ProbationRecord.builder()
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
                .map(this::convictionFrom)
                .collect(Collectors.toList());
    }

    public Conviction convictionFrom(CommunityApiConvictionResponse conviction) {
        return Conviction.builder()
                .convictionId(conviction.getConvictionId())
                .active(conviction.getActive())
                .inBreach(conviction.getInBreach())
                .convictionDate(conviction.getConvictionDate())
                .offences(conviction.getOffences().stream()
                    .map(offence -> new Offence(offence.getDetail().getDescription()))
                    .collect(Collectors.toList())
                )
                .sentence(Optional.ofNullable(conviction.getSentence()).map(this::buildSentence).orElse(null))
                .endDate(endDateCalculator.apply(conviction.getConvictionDate(), conviction.getSentence()))
                .build();
    }

    public CurrentOrderHeaderResponse buildCurrentOrderHeaderDetail(CommunityApiCurrentOrderHeaderDetailResponse currentOrderHeaderDetail) {
        return CurrentOrderHeaderResponse.builder()
                .sentenceId(currentOrderHeaderDetail.getSentenceId())
                .custodialType(currentOrderHeaderDetail.getCustodialType())
                .sentenceDescription(currentOrderHeaderDetail.getSentence().getDescription())
                .mainOffenceDescription(currentOrderHeaderDetail.getMainOffence().getDescription())
                .sentenceDate(currentOrderHeaderDetail.getSentenceDate())
                .actualReleaseDate(currentOrderHeaderDetail.getActualReleaseDate())
                .licenceExpiryDate(currentOrderHeaderDetail.getLicenceExpiryDate())
                .pssEndDate(currentOrderHeaderDetail.getPssEndDate())
                .length(currentOrderHeaderDetail.getLength())
                .lengthUnits(currentOrderHeaderDetail.getLengthUnits())
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
                .status(communityApiUnpaidWork.getStatus())
                .build();
    }

    final BiFunction<LocalDate, CommunityApiSentence, LocalDate> endDateCalculator = (convictionDate, sentence) -> {

        if (convictionDate == null || sentence == null) {
            return null;
        }

        return convictionDate.plus(sentence.getLengthInDays(), ChronoUnit.DAYS);
    };


}
