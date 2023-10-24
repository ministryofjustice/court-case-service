package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import uk.gov.justice.probation.courtcaseservice.controller.model.Verdict;
import uk.gov.justice.probation.courtcaseservice.controller.model.VerdictType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "VERDICT")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@Audited
public class VerdictEntity extends BaseAuditedEntity implements Serializable {


    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "TYPE_DESCRIPTION")
    private String typeDescription;

    @Column(name = "DATE")
    private LocalDate date;

    public static Verdict of(VerdictEntity verdictEntity) {
        return Verdict.builder()
                .verdictType(VerdictType.builder().description(verdictEntity.getTypeDescription()).build())
                .date(verdictEntity.getDate())
                .build();
    }

}
