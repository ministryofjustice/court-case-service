package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEventType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Schema(description = "Case Information")
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtCaseResponse {

    public static final String POSSIBLE_NDELIUS_RECORD_PROBATION_STATUS = "Possible NDelius record";

    private final String caseId;
    private final String hearingId;
    private final String hearingType;
    private final String caseNo;
    private final String crn;
    private final String urn;
    private final String pnc;
    private final String cro;
    private final String listNo;
    private final String courtCode;
    private final String courtRoom;
    private final String source;
    private final LocalDateTime sessionStartTime;
    private final CourtSession session;
    private final DefendantProbationStatus probationStatus;
    private final LocalDate previouslyKnownTerminationDate;
    private final Boolean suspendedSentenceOrder;
    private final Boolean breach;
    private final Boolean preSentenceActivity;
    private final List<OffenceResponse> offences;
    private final String defendantName;
    private final String defendantSurname;
    private final String defendantForename;
    private final NamePropertiesEntity name;
    private final AddressPropertiesEntity defendantAddress;
    private final LocalDate defendantDob;
    private final Sex defendantSex;
    private final DefendantType defendantType;
    private final String defendantId;
    private final String nationality1;
    private final String nationality2;
    private final PhoneNumber phoneNumber;
    private final boolean createdToday;
    private final boolean removed;
    private final long numberOfPossibleMatches;
    private final Boolean awaitingPsr;
    private final List<CaseProgressHearing> hearings;
    private final List<CaseCommentResponse> caseComments;
    private final List<CaseDocumentResponse> files;
    private final HearingEventType hearingEventType;
    private final Boolean confirmedOffender;
    private final String personId;
    private final List<CaseMarker> caseMarkers;
    private final HearingPrepStatus hearingPrepStatus;

    @JsonProperty
    public String getProbationStatus() {
        if(probationStatus == DefendantProbationStatus.UNCONFIRMED_NO_RECORD && numberOfPossibleMatches >= 1) {
            return POSSIBLE_NDELIUS_RECORD_PROBATION_STATUS;
        }
        return probationStatus.getName();
    }

    @JsonProperty
    public String getDefendantSex() {
        return Optional.ofNullable(defendantSex)
            .orElse(Sex.NOT_KNOWN)
            .getName();
    }

    @JsonProperty
    public String getProbationStatusActual() {
        return Optional.ofNullable(probationStatus)
            .map(DefendantProbationStatus::name)
            .orElse(null);
    }
}


