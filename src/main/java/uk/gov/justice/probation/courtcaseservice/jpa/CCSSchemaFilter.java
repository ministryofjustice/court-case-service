package uk.gov.justice.probation.courtcaseservice.jpa;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.spi.SchemaFilter;

@Slf4j
public class CCSSchemaFilter implements SchemaFilter {

    public static final CCSSchemaFilter INSTANCE = new CCSSchemaFilter();

    @Override
    public boolean includeNamespace(Namespace namespace) {
        return true;
    }

    @Override
    public boolean includeTable(Table table) {
        String tableName = table.getContributor().toUpperCase();
        boolean isEnversAuditTable = tableName.endsWith("_AUD") || tableName.equalsIgnoreCase("REVINFO"); // only allow envers audit tables to be auto created/updated by Hibernate
        log.info("Auto creating/updating table {}? Ans. {}", tableName, isEnversAuditTable);
        return isEnversAuditTable;
    }

    @Override
    public boolean includeSequence(Sequence sequence) {
        return true;
    }
}