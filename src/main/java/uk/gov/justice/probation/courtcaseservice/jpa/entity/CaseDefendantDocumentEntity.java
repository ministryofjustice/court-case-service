package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "CASE_DEFENDANT_DOCUMENTS")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@With
@Getter
@ToString
@Audited
public class CaseDefendantDocumentEntity {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Setter
    @ManyToOne(optional = false, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "FK_CASE_DEFENDANT_ID", referencedColumnName = "id", nullable = false)
    private CaseDefendantEntity caseDefendant;

    @Column(name = "DOCUMENT_ID", nullable = false)
    private String documentId;

    @Column(name = "DOCUMENT_NAME", nullable = false)
    private String documentName;
}
