package uk.gov.justice.probation.courtcaseservice.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;


    @Component
    public class PostgresHealthIndicator extends AbstractHealthIndicator {

        @Autowired
        private DataSource postgresDataSource;

        public DataSourceHealthIndicator dbHealthIndicator() {
            DataSourceHealthIndicator indicator = new DataSourceHealthIndicator(postgresDataSource);
            return indicator;
        }

        @Override
        protected void doHealthCheck(Health.Builder builder) throws Exception {
            Health h = dbHealthIndicator().health();
            Status status = h.getStatus();
            if (status != null && "DOWN".equals(status.getCode())) {
                builder.down();
            } else {
                builder.up();
            }
        }
    }

