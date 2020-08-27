package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.controller.model.Address;
import uk.gov.justice.probation.courtcaseservice.controller.model.CurrentOrderHeaderResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.Event;
import uk.gov.justice.probation.courtcaseservice.controller.model.MatchIdentifiers;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetail;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiContactDetails;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCustodialStatusResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderManager;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRequirementResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiSentence;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiUnpaidWork;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Offence;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderManager;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;


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

    public OffenderDetail offenderDetailFrom(CommunityApiOffenderResponse offenderResponse) {
        return OffenderDetail.builder()
            .otherIds(offenderResponse.getOtherIds())
            .probationStatus(offenderResponse.isCurrentDisposal() ? ProbationStatus.CURRENT : ProbationStatus.PREVIOUSLY_KNOWN)
            .dateOfBirth(offenderResponse.getDateOfBirth())
            .forename(offenderResponse.getFirstName())
            .middleNames(offenderResponse.getMiddleNames())
            .surname(offenderResponse.getSurname())
            .title(offenderResponse.getTitle())
            .build();
    }

    public OffenderMatchDetail offenderMatchDetailFrom(CommunityApiOffenderResponse offenderResponse, String addressCode) {
        return OffenderMatchDetail.builder()
            .title(offenderResponse.getTitle())
            .forename(offenderResponse.getFirstName())
            .middleNames(Optional.ofNullable(offenderResponse.getMiddleNames()).orElse(Collections.emptyList()))
            .surname(offenderResponse.getSurname())
            .address(addressFrom(offenderResponse.getContactDetails(), addressCode))
            .probationStatus(offenderResponse.isCurrentDisposal() ? ProbationStatus.CURRENT : ProbationStatus.PREVIOUSLY_KNOWN)
            .matchIdentifiers(Optional.ofNullable(offenderResponse.getOtherIds())
                .map(otherIds -> MatchIdentifiers.builder().crn(otherIds.getCrn())
                                                            .cro(otherIds.getCroNumber())
                                                            .pnc(otherIds.getPncNumber())
                                                            .build())
                .orElse(null)
            )
            .dateOfBirth(offenderResponse.getDateOfBirth())
            .build();
    }

    public OffenderMatchDetail offenderMatchDetailFrom(OffenderMatchDetail offenderMatchDetail, Sentence sentence) {
        OffenderMatchDetail.OffenderMatchDetailBuilder builder = OffenderMatchDetail.builder()
            .title(offenderMatchDetail.getTitle())
            .forename(offenderMatchDetail.getForename())
            .middleNames(Optional.ofNullable(offenderMatchDetail.getMiddleNames()).orElse(Collections.emptyList()))
            .surname(offenderMatchDetail.getSurname())
            .address(offenderMatchDetail.getAddress())
            .probationStatus(offenderMatchDetail.getProbationStatus())
            .matchIdentifiers(offenderMatchDetail.getMatchIdentifiers())
            .dateOfBirth(offenderMatchDetail.getDateOfBirth());
        if (sentence != null) {
            builder.event(Event.builder()
                .text(sentence.getDescription())
                .length(sentence.getLength())
                .lengthUnits(sentence.getLengthUnits())
                .startDate(sentence.getStartDate())
                .build());
        }
        return builder.build();
    }

    private Address addressFrom(CommunityApiContactDetails contactDetails, String addressCode) {
        if (contactDetails == null || contactDetails.getAddresses() == null) {
            return null;
        }
        return contactDetails.getAddresses().stream()
            .filter(address -> address.getAddressTypeDetail() != null && address.getAddressTypeDetail().getCode().equalsIgnoreCase(addressCode))
            .findFirst()
            .map(address -> Address.builder().addressNumber(address.getAddressNumber())
                                            .town(address.getTown())
                                            .buildingName(address.getBuildingName())
                                            .streetName(address.getStreetName())
                                            .district(address.getDistrict())
                                            .county(address.getCounty())
                                            .postcode(address.getPostcode())
                                        .build())
            .orElse(null);
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
                .offences(Optional.ofNullable(conviction.getOffences()).orElse(Collections.emptyList()).stream()
                    .map(offence -> new Offence(offence.getDetail().getDescription()))
                    .collect(Collectors.toList())
                )
                .sentence(Optional.ofNullable(conviction.getSentence()).map(this::buildSentence).orElse(null))
                .endDate(endDateCalculator.apply(conviction.getConvictionDate(), conviction.getSentence()))
                .build();
    }

    public CurrentOrderHeaderResponse buildCurrentOrderHeaderDetail(CommunityApiCustodialStatusResponse custodialStatusResponse) {
        return CurrentOrderHeaderResponse.builder()
                .sentenceId(custodialStatusResponse.getSentenceId())
                .custodialType(custodialStatusResponse.getCustodialType())
                .sentenceDescription(custodialStatusResponse.getSentence() != null ? custodialStatusResponse.getSentence().getDescription() : null)
                .mainOffenceDescription(custodialStatusResponse.getMainOffence() != null ?custodialStatusResponse.getMainOffence().getDescription() : null)
                .sentenceDate(custodialStatusResponse.getSentenceDate())
                .actualReleaseDate(custodialStatusResponse.getActualReleaseDate())
                .licenceExpiryDate(custodialStatusResponse.getLicenceExpiryDate())
                .pssEndDate(custodialStatusResponse.getPssEndDate())
                .length(custodialStatusResponse.getLength())
                .lengthUnits(custodialStatusResponse.getLengthUnits())
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
                .active(requirement.getActive() != null && requirement.getActive())
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
            .sentenceId(communityApiSentence.getSentenceId() != null ? communityApiSentence.getSentenceId().toString() : null)
            .description(communityApiSentence.getDescription())
            .length(communityApiSentence.getOriginalLength())
            .lengthUnits(communityApiSentence.getOriginalLengthUnits())
            .lengthInDays(communityApiSentence.getLengthInDays())
            .startDate(communityApiSentence.getStartDate())
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

        if (convictionDate == null || sentence == null || sentence.getLengthInDays() == null) {
            return null;
        }

        return convictionDate.plus(sentence.getLengthInDays(), ChronoUnit.DAYS);
    };



}
