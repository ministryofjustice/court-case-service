package uk.gov.justice.probation.courtcaseservice.jpa;

import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.SchemaFilterProvider;
import org.springframework.stereotype.Component;

@Component
public class CCSSchemaFilterProvider implements SchemaFilterProvider {

    @Override
    public SchemaFilter getCreateFilter() {
        return CCSSchemaFilter.INSTANCE;
    }

    @Override
    public SchemaFilter getDropFilter() {
        return CCSSchemaFilter.INSTANCE;
    }

    @Override
    public SchemaFilter getTruncatorFilter() { return CCSSchemaFilter.INSTANCE; }

    @Override
    public SchemaFilter getMigrateFilter() {
        return CCSSchemaFilter.INSTANCE;
    }

    @Override
    public SchemaFilter getValidateFilter() {
        return CCSSchemaFilter.INSTANCE;
    }
}


