package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.justice.probation.courtcaseservice.service.model.MatchType;

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

@ApiModel(description = "Offender Match")
@Entity
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
    private Long id;

    @Column(name = "CRN", nullable = false)
    private String crn;

    @Column(name = "PNC")
    private String pnc;

    @Column(name = "CRO")
    private String cro;

    @Column(name = "MATCH_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private MatchType matchType;

    @Column(name = "CONFIRMED", nullable = false)
    private Boolean confirmed;

    @JoinColumn(name = "GROUP_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(optional = false)
    @JsonIgnore
    private GroupedOffenderMatchesEntity group;
}
