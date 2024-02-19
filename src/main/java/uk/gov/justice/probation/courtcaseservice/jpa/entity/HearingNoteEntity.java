package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "HEARING_NOTES")
@SQLDelete(sql = "UPDATE HEARING_NOTES SET deleted = true WHERE ID = ?")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@Getter
@With
@EqualsAndHashCode(callSuper = false)
public class HearingNoteEntity extends BaseEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "HEARING_ID", updatable = false, nullable = false)
    private final String hearingId;

    @ManyToOne
    @JoinColumn(name = "FK_HEARING_DEFENDANT_ID", referencedColumnName = "id")
    @Setter
    @JsonIgnore
    @ToString.Exclude
    private HearingDefendantEntity hearingDefendant;

    @Column(name = "CREATED_BY_UUID", updatable = false, nullable = false)
    private final String createdByUuid;

    @Column(name = "NOTE", nullable = false)
    private String note;

    @Column(name = "AUTHOR", nullable = false)
    private final String author;

    @Column(name = "DRAFT", nullable = false)
    private boolean draft;

    public void updateNote(HearingNoteEntity hearingNoteUpdate) {
        this.note = hearingNoteUpdate.getNote();
        this.draft = hearingNoteUpdate.isDraft();
        this.setDeleted(false); // to reuse note drafts that were soft deleted earlier. Remove deleted flag.
    }
}
