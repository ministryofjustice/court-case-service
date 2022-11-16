package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

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
@Table(name = "JUDICIAL_RESULT")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@Audited
public class JudicialResultEntity extends BaseImmutableEntity implements Serializable {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "LABEL")
    private final String label;

    @Column(name="IS_CONVICTED_RESULT")
    private boolean isConvictedResult;

    @Column(name = "JUDICIAL_RESULT_TYPE_ID")
    private final String judicialResultTypeId;

    @ManyToOne
    @JoinColumn(name = "OFFENCE_ID", referencedColumnName = "id")
    @Setter
    private OffenceEntity offence;


}
