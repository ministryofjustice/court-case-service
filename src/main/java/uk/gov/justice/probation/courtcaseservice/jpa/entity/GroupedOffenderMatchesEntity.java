package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Schema(description = "Grouped Offender Matches")
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
@SuperBuilder
@Getter
@Table(name = "OFFENDER_MATCH_GROUP")
public class GroupedOffenderMatchesEntity extends BaseEntity implements Serializable {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Setter
    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER, cascade = { CascadeType.ALL }, orphanRemoval=true)
    @OrderBy("crn ASC")
    @LazyCollection(value = LazyCollectionOption.FALSE)
    private List<OffenderMatchEntity> offenderMatches;

    @Setter
    @Column(name = "CASE_ID", nullable = false)
    private String caseId;

    @Setter
    @Column(name = "DEFENDANT_ID", nullable = false)
    private String defendantId;

    public void clearOffenderMatches() {
        if (this.offenderMatches != null) {
            for (OffenderMatchEntity offenderMatchEntity : this.offenderMatches) {
                offenderMatchEntity.setGroup(null);
            }
            this.offenderMatches.clear();
        }
    }

    private Optional<OffenderMatchEntity> findMatchByCrn(String crn) {
        return getOffenderMatches().stream().filter(offenderMatchEntity -> crn.equalsIgnoreCase(offenderMatchEntity.getCrn())).findAny();
    }
    public void updateMatches(List<OffenderMatchEntity> newMatches) {

        var removalList = getOffenderMatches().stream()
            .filter(offenderMatchEntity -> newMatches.stream().filter(newMatch -> newMatch.getCrn().equalsIgnoreCase(offenderMatchEntity.getCrn()))
                .findAny().isEmpty()).collect(Collectors.toList());
        
        getOffenderMatches().removeAll(removalList);

        newMatches.forEach(matchRequest -> {
            findMatchByCrn(matchRequest.getCrn()).ifPresentOrElse(existing -> {
                existing.update(matchRequest);
            }, () -> {
                getOffenderMatches().add(matchRequest);
                matchRequest.setGroup(this);
            } );
        });
    }
}
