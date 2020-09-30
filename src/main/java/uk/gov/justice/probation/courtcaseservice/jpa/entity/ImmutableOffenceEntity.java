package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "IMMUTABLE_OFFENCE")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor
@Getter
@ToString(exclude = "courtCase")
public class ImmutableOffenceEntity extends BaseImmutableEntity implements Serializable  {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "COURT_CASE_ID", referencedColumnName = "id")
    @JsonProperty(access = Access.WRITE_ONLY)
    @JsonBackReference
    private CourtCaseEntity courtCase;

    @Column(name = "OFFENCE_TITLE")
    private String offenceTitle;

    @Column(name = "OFFENCE_SUMMARY")
    private String offenceSummary;

    @Column(name = "ACT")
    private String act;

    @OrderColumn
    @Column(name = "SEQUENCE_NUMBER", nullable = false)
    @JsonProperty
    private Integer sequenceNumber;
}
