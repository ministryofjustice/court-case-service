package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "OFFENCE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OffenceEntity {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @ManyToOne
    @JoinColumn(name="CASE_ID", nullable = false, referencedColumnName = "CASE_ID")
    @JsonIgnore
    @Setter
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
