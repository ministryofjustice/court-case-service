package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseMarker;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "CASE_MARKER")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@Audited
public class CaseMarkerEntity extends BaseAuditedEntity implements Serializable {


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
    private CourtCaseEntity courtCase;

    public static CaseMarker of(CaseMarkerEntity caseMarkerEntity){
        return CaseMarker.builder()
                .typeDescription(caseMarkerEntity.getTypeDescription())
                .build();

    }


}
