package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import uk.gov.justice.probation.courtcaseservice.controller.model.Plea;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "PLEA")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@Audited
public class PleaEntity extends BaseAuditedEntity implements Serializable {


    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "VALUE")
    private String value;

    @Column(name = "DATE")
    private LocalDate date;

    public static Plea of(PleaEntity pleaEntity) {
        return Plea.builder()
                .pleaValue(pleaEntity.getValue())
                .pleaDate(pleaEntity.getDate())
                .build();
    }

}
