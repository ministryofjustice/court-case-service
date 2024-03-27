package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "CASE_COMMENTS")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@Getter
@With
@EqualsAndHashCode(callSuper = false)
public class CaseCommentEntity extends BaseEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "CASE_ID", updatable = false, nullable = false)
    private final String caseId;

    @Column(name = "DEFENDANT_ID", updatable = false, nullable = false)
    private final String defendantId;

    @Column(name = "CREATED_BY_UUID", updatable = false, nullable = false)
    private final String createdByUuid;

    @Column(name = "COMMENT", nullable = false)
    private String comment;

    @Column(name = "AUTHOR", nullable = false)
    private final String author;

    @Column(name = "IS_DRAFT", nullable = false)
    private boolean draft;

    @Column(name = "LEGACY", nullable = false)
    private boolean legacy;
    public void update(CaseCommentEntity caseCommentEntity) {
        this.comment = caseCommentEntity.getComment();
        this.draft = caseCommentEntity.isDraft();
        this.legacy = caseCommentEntity.isLegacy();
    }
}
