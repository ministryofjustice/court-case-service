package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
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

    @Column(name = "CREATED_BY_UUID", updatable = false, nullable = false)
    private final String createdByUuid;

    @Column(name = "NOTE", nullable = false)
    private String note;

    @Column(name = "AUTHOR", nullable = false)
    private final String author;

    public void updateNote(HearingNoteEntity hearingNoteUpdate) {
        this.note = hearingNoteUpdate.getNote();
    }
}
