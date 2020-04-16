package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.*;
import uk.gov.justice.probation.courtcaseservice.service.model.*;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.justice.probation.courtcaseservice.service.model.document.ConvictionDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

@Component
public class OffenderMapper {
    public ProbationRecord offenderFrom(CommunityApiOffenderResponse offenderResponse) {
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

    public GroupedDocuments documentsFrom(CommunityApiGroupedDocumentsResponse documentsResponse) {
        return GroupedDocuments.builder()
            .documents(Optional.ofNullable(documentsResponse.getDocuments()).map(this::buildDocuments).orElse(Collections.emptyList()))
            .convictions(Optional.ofNullable(documentsResponse.getConvictions()).map(this::buildConvictionDocuments).orElse(Collections.emptyList()))
            .build();
    }

    private List<ConvictionDocuments> buildConvictionDocuments(List<CommunityApiConvictionDocuments> convictionDocuments) {
        return convictionDocuments.stream()
                .map(this::buildConvictionDocument)
                .collect(Collectors.toList());
    }

    private ConvictionDocuments buildConvictionDocument(CommunityApiConvictionDocuments convictionDocuments) {
        return ConvictionDocuments.builder()
            .convictionId(convictionDocuments.getConvictionId())
            .documents(Optional.ofNullable(convictionDocuments.getDocuments()).map(this::buildConvictionIdDocuments).orElse(Collections.emptyList()))
            .build();
    }

    private List<OffenderDocumentDetail> buildConvictionIdDocuments(List<CommunityApiOffenderDocumentDetail> documentDetails) {
        return documentDetails.stream()
            .map(this::buildOffenderDocumentDetail)
            .collect(Collectors.toList());
    }

    private List<OffenderDocumentDetail> buildDocuments(List<CommunityApiOffenderDocumentDetail> details) {
        return details.stream()
            .map(this::buildOffenderDocumentDetail)
            .collect(Collectors.toList());
    }

    private OffenderDocumentDetail buildOffenderDocumentDetail(CommunityApiOffenderDocumentDetail detail) {
        return OffenderDocumentDetail.builder()
            .documentId(detail.getId())
            .documentName(detail.getDocumentName())
            .author(detail.getAuthor())
            .type(DocumentType.valueOf(detail.getType().getCode()))
            .extendedDescription(detail.getExtendedDescription())
            .createdAt(detail.getCreatedAt())
            .reportDocumentDates(detail.getReportDocumentDates())
            .subType(detail.getSubType())
            .build();
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
