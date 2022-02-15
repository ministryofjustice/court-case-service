package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Immutable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Immutable
public class BaseImmutableEntity {

    @Column(name = "created", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime created;

    @Column(name = "created_by", nullable = true, updatable = false)
    @CreatedBy
    private String createdBy;

    protected BaseImmutableEntity(BaseImmutableEntityBuilder<?, ?> b) {
        this.created = b.created;
        this.createdBy = b.createdBy;
    }

    public static BaseImmutableEntityBuilder<?, ?> builder() {
        return new BaseImmutableEntityBuilderImpl();
    }

    public static abstract class BaseImmutableEntityBuilder<C extends BaseImmutableEntity, B extends BaseImmutableEntityBuilder<C, B>> {
        private LocalDateTime created;
        private String createdBy;

        public B created(LocalDateTime created) {
            this.created = created;
            return self();
        }

        public B createdBy(String createdBy) {
            this.createdBy = createdBy;
            return self();
        }

        protected abstract B self();

        public abstract C build();

        public String toString() {
            return "BaseImmutableEntity.BaseImmutableEntityBuilder(created=" + this.created + ", createdBy=" + this.createdBy + ")";
        }
    }

    private static final class BaseImmutableEntityBuilderImpl extends BaseImmutableEntityBuilder<BaseImmutableEntity, BaseImmutableEntityBuilderImpl> {
        private BaseImmutableEntityBuilderImpl() {
        }

        protected BaseImmutableEntityBuilderImpl self() {
            return this;
        }

        public BaseImmutableEntity build() {
            return new BaseImmutableEntity(this);
        }
    }
}
