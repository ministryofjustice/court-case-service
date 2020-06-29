package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

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
import javax.persistence.Table;

@ApiModel(description = "Offender Match")
@Entity
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@Data
@Table(name = "OFFENDER_MATCH")
public class OffenderMatchEntity {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "CASE_NO", referencedColumnName = "case_no", nullable = false),
            @JoinColumn(name = "COURT_CODE", referencedColumnName = "court_code", nullable = false),
    })
    @JsonIgnore
    private CourtCaseEntity courtCaseEntity;

    @Column(name = "CASE_NO", nullable = false, insertable = false, updatable = false)
    private String caseNo;

    @Column(name = "COURT_CODE", nullable = false, insertable = false, updatable = false)
    private String courtCode;

    @Column(name = "CRN", nullable = false)
    private String crn;

    @Column(name = "PNC")
    private String pnc;

    @Column(name = "CRO")
    private String cro;

    @Column(name = "MATCH_TYPE", nullable = false)
    private String matchType;

    @Column(name = "CONFIRMED", nullable = false)
    private Boolean confirmed;
}
