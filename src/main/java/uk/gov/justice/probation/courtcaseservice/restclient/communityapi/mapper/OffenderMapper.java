package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import uk.gov.justice.probation.courtcaseservice.controller.model.Address;
import uk.gov.justice.probation.courtcaseservice.controller.model.Event;
import uk.gov.justice.probation.courtcaseservice.controller.model.MatchIdentifiers;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetail;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiContactDetails;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiConvictionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCustody;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffence;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenceDetail;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiSentence;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiSentenceStatusResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiUnpaidWork;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Offence;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;
import uk.gov.justice.probation.courtcaseservice.service.model.SentenceStatus;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OffenderMapper {

    public static ProbationStatusDetail probationStatusDetailFrom(CommunityApiProbationStatusDetail probationStatusDetail) {
        return ProbationStatusDetail.builder()
            .status(probationStatusDetail.getStatus())
            .previouslyKnownTerminationDate(probationStatusDetail.getPreviouslyKnownTerminationDate())
            .preSentenceActivity(probationStatusDetail.getPreSentenceActivity())
            .inBreach(probationStatusDetail.getInBreach())
            .awaitingPsr(probationStatusDetail.getAwaitingPsr())
            .build();
    }

    public static OffenderDetail offenderDetailFrom(CommunityApiOffenderResponse offenderResponse, ProbationStatusDetail probationStatusDetail) {
        return OffenderDetail.builder()
            .otherIds(otherIdsFrom(offenderResponse))
            .probationStatus(ProbationStatus.of(probationStatusDetail.getStatus()))
            .dateOfBirth(offenderResponse.getDateOfBirth())
            .forename(offenderResponse.getFirstName())
            .middleNames(offenderResponse.getMiddleNames())
            .surname(offenderResponse.getSurname())
            .title(offenderResponse.getTitle())
            .build();
    }

    private static uk.gov.justice.probation.courtcaseservice.service.model.OtherIds otherIdsFrom(CommunityApiOffenderResponse offenderResponse) {
        return uk.gov.justice.probation.courtcaseservice.service.model.OtherIds.builder()
                .crn(offenderResponse.getOtherIds().getCrn())
                .offenderId(offenderResponse.getOffenderId())
                .pncNumber(offenderResponse.getOtherIds().getPncNumber())
                .croNumber(offenderResponse.getOtherIds().getCroNumber())
                .build();
    }

    public static OffenderMatchDetail offenderMatchDetailFrom(CommunityApiOffenderResponse offenderResponse, String addressCode) {
        return OffenderMatchDetail.builder()
            .title(offenderResponse.getTitle())
            .forename(offenderResponse.getFirstName())
            .middleNames(Optional.ofNullable(offenderResponse.getMiddleNames()).orElse(Collections.emptyList()))
            .surname(offenderResponse.getSurname())
            .address(addressFrom(offenderResponse.getContactDetails(), addressCode))
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

    public static OffenderMatchDetail offenderMatchDetailFrom(OffenderMatchDetail offenderMatchDetail, Sentence sentence, ProbationStatusDetail probationStatus) {
        OffenderMatchDetail.OffenderMatchDetailBuilder builder = OffenderMatchDetail.builder()
            .title(offenderMatchDetail.getTitle())
            .forename(offenderMatchDetail.getForename())
            .middleNames(Optional.ofNullable(offenderMatchDetail.getMiddleNames()).orElse(Collections.emptyList()))
            .surname(offenderMatchDetail.getSurname())
            .address(offenderMatchDetail.getAddress())
            .probationStatus(ProbationStatus.of(probationStatus.getStatus()))
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

    private static Address addressFrom(CommunityApiContactDetails contactDetails, String addressCode) {
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

    public static List<Conviction> convictionsFrom(CommunityApiConvictionsResponse convictionsResponse) {
        return convictionsResponse.getConvictions().stream()
            .map(OffenderMapper::convictionFrom)
            .collect(Collectors.toList());
    }

    public static Conviction convictionFrom(CommunityApiConvictionResponse conviction) {
        return Conviction.builder()
                .convictionId(conviction.getConvictionId())
                .active(conviction.getActive())
                .inBreach(conviction.getInBreach())
                .awaitingPsr(Optional.ofNullable(conviction.getAwaitingPsr()).orElse(Boolean.FALSE))
                .convictionDate(conviction.getConvictionDate())
                .custodialType(Optional.ofNullable(conviction.getCustody()).map(CommunityApiCustody::getStatus).orElse(null))
                .offences(Optional.ofNullable(conviction.getOffences()).orElse(Collections.emptyList()).stream()
                    .map(OffenderMapper::offenceFrom)
                    .collect(Collectors.toList())
                )
                .sentence(Optional.ofNullable(conviction.getSentence()).map(OffenderMapper::buildSentence).orElse(null))
                .endDate(endDateCalculator(conviction.getSentence()))
                .build();
    }

    private static Offence offenceFrom(CommunityApiOffence offence) {
        return Offence.builder()
            .offenceDate(offence.getOffenceDate())
            .main(offence.isMainOffence())
            .description(Optional.ofNullable(offence.getDetail()).map(CommunityApiOffenceDetail::getDescription).orElse(null))
            .build();
    }

    public static SentenceStatus buildSentenceStatus(CommunityApiSentenceStatusResponse custodialStatusResponse) {
        return SentenceStatus
                .builder()
                .sentenceId(custodialStatusResponse.getSentenceId())
                .custodialType(custodialStatusResponse.getCustodialType())
                .sentenceDescription(custodialStatusResponse.getSentence() != null ? custodialStatusResponse.getSentence().getDescription() : null)
                .mainOffenceDescription(custodialStatusResponse.getMainOffence() != null ? custodialStatusResponse.getMainOffence().getDescription() : null)
                .sentenceDate(custodialStatusResponse.getSentenceDate())
                .actualReleaseDate(custodialStatusResponse.getActualReleaseDate())
                .licenceExpiryDate(custodialStatusResponse.getLicenceExpiryDate())
                .pssEndDate(custodialStatusResponse.getPssEndDate())
                .length(custodialStatusResponse.getLength())
                .lengthUnits(custodialStatusResponse.getLengthUnits())
                .build();
    }

    private static Sentence buildSentence(final CommunityApiSentence communityApiSentence) {
        return Sentence.builder()
            .sentenceId(communityApiSentence.getSentenceId() != null ? communityApiSentence.getSentenceId().toString() : null)
            .description(communityApiSentence.getDescription())
            .length(communityApiSentence.getOriginalLength())
            .lengthUnits(communityApiSentence.getOriginalLengthUnits())
            .lengthInDays(communityApiSentence.getLengthInDays())
            .startDate(communityApiSentence.getStartDate())
            .terminationDate(communityApiSentence.getTerminationDate())
            .terminationReason(communityApiSentence.getTerminationReason())
            .unpaidWork(Optional.ofNullable(communityApiSentence.getUnpaidWork()).map(OffenderMapper::buildUnpaidWork).orElse(null))
            .endDate(endDateCalculator(communityApiSentence))
            .build();
    }

    private static UnpaidWork buildUnpaidWork(final CommunityApiUnpaidWork communityApiUnpaidWork) {
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

    static LocalDate endDateCalculator(CommunityApiSentence sentence) {

        if (sentence == null || sentence.getStartDate() == null ||  sentence.getLengthInDays() == null) {
            return null;
        }

        return sentence.getStartDate().plus(sentence.getLengthInDays(), ChronoUnit.DAYS);
    }
}
