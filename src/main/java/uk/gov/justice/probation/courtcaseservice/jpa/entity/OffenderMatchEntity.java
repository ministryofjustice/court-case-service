package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.gov.justice.probation.courtcaseservice.service.model.MatchType;

@ApiModel(description = "Offender Match")
@Entity
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@RequiredArgsConstructor
@SuperBuilder
@Getter
@Table(name = "OFFENDER_MATCH")
public class OffenderMatchEntity extends BaseEntity {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Long id;

    @Column(name = "CRN", nullable = false)
    @EqualsAndHashCode.Include
    private String crn;

    @Column(name = "PNC")
    @EqualsAndHashCode.Include
    private String pnc;

    @Column(name = "CRO")
    @EqualsAndHashCode.Include
    private String cro;

    @Column(name = "MATCH_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    @EqualsAndHashCode.Include
    private MatchType matchType;

    @Setter
    @Column(name = "CONFIRMED", nullable = false)
    @EqualsAndHashCode.Include
    private Boolean confirmed;

    @Setter
    @Column(name = "REJECTED", nullable = false)
    @EqualsAndHashCode.Include
    private Boolean rejected;

    @JoinColumn(name = "GROUP_ID", nullable = false)
    @ManyToOne(optional = false)
    @EqualsAndHashCode.Include
    @JsonIgnore
    private GroupedOffenderMatchesEntity group;

}
