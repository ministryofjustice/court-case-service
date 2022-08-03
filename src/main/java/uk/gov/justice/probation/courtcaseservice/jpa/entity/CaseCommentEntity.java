package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Entity
@Table(name = "CASE_COMMENTS")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@Getter
@With
@EqualsAndHashCode(callSuper = true)
public class CaseCommentEntity extends BaseImmutableEntity {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "COMMENT_ID", updatable = false, nullable = false)
    private final String commentId;

    @Column(name = "COMMENT", nullable = false)
    private final String comment;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_COURT_CASE_ID", referencedColumnName = "id", nullable = false)
    @Setter
    @JsonIgnore
    private CourtCaseEntity courtCase;

    @Column(name = "deleted", nullable = false, updatable = false)
    private final boolean deleted;

    public void setCourtCaseEntity(CourtCaseEntity courtCase) {
        this.courtCase = courtCase;
    }
}
