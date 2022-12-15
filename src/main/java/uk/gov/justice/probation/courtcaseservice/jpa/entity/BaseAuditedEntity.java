package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

import javax.persistence.MappedSuperclass;

@AllArgsConstructor
@Data
@SuperBuilder
@MappedSuperclass
@Audited
public class BaseAuditedEntity extends BaseEntity {
}
