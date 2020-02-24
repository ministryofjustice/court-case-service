package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "OFFENCE")
public class OffenceEntity {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @ManyToOne
    @JoinColumn(name="CASE_ID", nullable = false)
    @JsonIgnore
    private CourtCaseEntity courtCase;

    @Column(name = "OFFENCE_TITLE")
    private String offenceTitle;

    @Column(name = "OFFENCE_SUMMARY")
    private String offenceSummary;

    @Column(name = "ACT")
    private String act;

    @Column(name = "SEQUENCE_NUMBER")
    private Integer sequenceNumber;
}
