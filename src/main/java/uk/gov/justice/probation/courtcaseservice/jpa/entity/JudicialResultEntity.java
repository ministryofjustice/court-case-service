package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "JUDICIAL_RESULT")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@Audited
public class JudicialResultEntity extends BaseAuditedEntity implements Serializable {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "LABEL", columnDefinition = "TEXT")
    private final String label;

    @Column(name="IS_CONVICTED_RESULT")
    private boolean isConvictedResult;

    @Column(name = "JUDICIAL_RESULT_TYPE_ID", columnDefinition = "TEXT")
    private final String judicialResultTypeId;

    @ManyToOne
    @JoinColumn(name = "OFFENCE_ID", referencedColumnName = "id")
    @Setter
    private OffenceEntity offence;

    @Column(name = "RESULT_TEXT", columnDefinition = "TEXT")
    private final String resultText;
}
