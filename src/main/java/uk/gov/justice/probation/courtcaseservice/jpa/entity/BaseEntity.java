package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
@SuperBuilder
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    @Column(name = "created", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime created;

    @Column(name = "last_updated")
    @LastModifiedDate
    private LocalDateTime lastUpdated;

    @Column(name = "created_by", nullable = true, updatable = false)
    @CreatedBy
    private String createdBy;

    @Column(name = "last_updated_by")
    @LastModifiedBy
    private String lastUpdatedBy;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Version
    private int version;

}
