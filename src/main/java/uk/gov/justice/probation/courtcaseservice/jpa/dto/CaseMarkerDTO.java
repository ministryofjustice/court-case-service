package uk.gov.justice.probation.courtcaseservice.jpa.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseMarker;

@Entity
@Table(name = "CASE_MARKER")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
public class CaseMarkerDTO {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "TYPE_DESCRIPTION")
    private String typeDescription;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_COURT_CASE_ID", referencedColumnName = "id", nullable = false)
    @Setter
    private CourtCaseDTO courtCase;

    public static CaseMarker of(CaseMarkerDTO caseMarkerDTO){
        return CaseMarker.builder()
                .markerTypeDescription(caseMarkerDTO.getTypeDescription())
                .build();

    }
}