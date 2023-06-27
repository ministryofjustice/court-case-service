BEGIN;

ALTER TABLE defendant ADD COLUMN tsv_name tsvector;

UPDATE defendant set tsv_name = to_tsvector(defendant_name);

CREATE TRIGGER tsvector_update_defendant_tsv_name BEFORE INSERT OR UPDATE
    ON defendant FOR EACH ROW EXECUTE PROCEDURE
    tsvector_update_trigger(tsv_name, 'pg_catalog.english', defendant_name);

CREATE INDEX defendant_tsv_name ON defendant USING gin(tsv_name);

COMMIT;
