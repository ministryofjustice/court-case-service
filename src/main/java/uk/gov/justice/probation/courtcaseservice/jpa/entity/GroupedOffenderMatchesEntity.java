package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

@ApiModel(description = "Grouped Offender Matches")
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
@SuperBuilder
@Getter
@Table(name = "OFFENDER_MATCH_GROUP")
public class GroupedOffenderMatchesEntity extends BaseEntity implements Serializable {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Setter
    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER, cascade = { CascadeType.ALL }, orphanRemoval=true)
    private List<OffenderMatchEntity> offenderMatches;

    @Column(name = "CASE_NO")
    private String caseNo;

    @Column(name = "COURT_CODE")
    private String courtCode;

    public void clearOffenderMatches() {
        if (this.offenderMatches != null) {
            for (OffenderMatchEntity offenderMatchEntity : this.offenderMatches) {
                offenderMatchEntity.setGroup(null);
            }
            this.offenderMatches.clear();
        }
    }

}
