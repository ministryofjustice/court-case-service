package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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

    @Column(name = "CASE_NO", nullable = false)
    private String caseNo;

    @Column(name = "COURT_CODE", nullable = false)
    private String courtCode;
}
