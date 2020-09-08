package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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

    @EqualsAndHashCode.Include
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "CASE_NO", referencedColumnName = "case_no", nullable = false),
            @JoinColumn(name = "COURT_CODE", referencedColumnName = "court_code", nullable = false),
    })
    private CourtCaseEntity courtCase;

}
