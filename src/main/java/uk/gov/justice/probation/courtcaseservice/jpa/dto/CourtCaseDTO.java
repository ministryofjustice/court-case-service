package uk.gov.justice.probation.courtcaseservice.jpa.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.BaseAuditedEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;

import java.io.Serializable;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@ToString(doNotUseGetters = true)
@Getter
@SuperBuilder
@With
@Table(name = "COURT_CASE")
public class CourtCaseDTO extends BaseAuditedEntity implements Serializable {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "CASE_ID", nullable = false)
    private final String caseId;

    @Column(name = "CASE_NO", nullable = false)
    private final String caseNo;

    @Column(name = "urn", nullable = false)
    private String urn;

    @Column(name = "SOURCE_TYPE")
    @Enumerated(EnumType.STRING)
    private final SourceType sourceType;

    @ToString.Exclude
    @JsonIgnore
    @Setter
    @OneToMany(mappedBy = "courtCase", fetch = FetchType.LAZY)
    private List<CaseMarkerDTO> caseMarkers;

}