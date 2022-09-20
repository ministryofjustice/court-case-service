package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "HEARING_NOTES")
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
    private final String note;

    @Column(name = "AUTHOR", nullable = false)
    private final String author;
}
