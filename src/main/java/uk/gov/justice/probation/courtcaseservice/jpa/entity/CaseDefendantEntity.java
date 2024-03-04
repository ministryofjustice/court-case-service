package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.Audited;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "CASE_DEFENDANT")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@With
@Getter
@ToString
@EqualsAndHashCode(callSuper = true, exclude = {"documents", "courtCase", "defendant" })
@Audited
public class CaseDefendantEntity extends BaseAuditedEntity implements Serializable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @ToString.Exclude
    @ManyToOne()
    @JoinColumn(name = "FK_COURT_CASE_ID", referencedColumnName = "id")
    @Setter
    private CourtCaseEntity courtCase;

    @Setter
    @OneToOne(optional = false, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "FK_CASE_DEFENDANT_ID", referencedColumnName = "id", nullable = false)
    private DefendantEntity defendant;

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "caseDefendant", cascade = CascadeType.ALL, orphanRemoval=true)
    private List<CaseDefendantDocumentEntity> documents;

    public CaseDefendantDocumentEntity getCaseDefendantDocument(String documentId) {
        return this.documents.stream().filter(caseDefendantDocumentEntity -> StringUtils.equals(documentId, caseDefendantDocumentEntity.getDocumentId()))
            .findAny().orElse(null);
    }

    public CaseDefendantDocumentEntity createDocument(String documentUuid, String fileName) {
        CaseDefendantDocumentEntity caseDefendantDocumentEntity = CaseDefendantDocumentEntity.builder()
            .caseDefendant(this)
            .documentId(documentUuid)
            .documentName(fileName)
            .created(LocalDateTime.now())
            .build();
        var caseDefendantDocumentEntities = Optional.ofNullable(this.getDocuments())
            .orElseGet(() -> {
                this.documents = new ArrayList<>();
                return this.documents;
            });
        caseDefendantDocumentEntities.add(caseDefendantDocumentEntity);
        return caseDefendantDocumentEntity;
    }
}
