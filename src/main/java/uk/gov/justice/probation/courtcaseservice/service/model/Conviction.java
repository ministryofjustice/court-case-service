package uk.gov.justice.probation.courtcaseservice.service.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

@ApiModel("Conviction")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Conviction implements Comparable<Conviction>{
    private final String convictionId;
    private final Boolean active;
    private final Boolean inBreach;
    private final LocalDate convictionDate;
    private final List<Offence> offences;
    private final Sentence sentence;
    private final LocalDate endDate;
    private final KeyValue custodialType;
    @Setter
    private List<OffenderDocumentDetail> documents;
    @Setter
    private List<Breach> breaches;
    @Setter
    private List<Requirement> requirements;
    @Setter
    private List<PssRequirement> pssRequirements;
    @Setter
    private List<LicenceCondition> licenceConditions;

    @Override
    public int compareTo(Conviction other) {
        return ((Long)Long.parseLong(convictionId)).compareTo(Long.parseLong(other.getConvictionId()));
    }

    @JsonIgnore
    public Optional<LocalDate> getSentenceStartDate() {
        return Optional.ofNullable(sentence).map(Sentence::getStartDate);
    }

    @JsonIgnore
    public Optional<LocalDate> getSentenceTerminationDate() {
        return Optional.ofNullable(sentence).map(Sentence::getTerminationDate);
    }
}
