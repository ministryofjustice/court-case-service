drop trigger if exists tsvector_update_defendant_tsv_name on defendant;

CREATE TRIGGER tsvector_update_defendant_tsv_name BEFORE INSERT OR UPDATE
   ON defendant FOR EACH ROW EXECUTE PROCEDURE
   tsvector_update_trigger(tsv_name, 'pg_catalog.simple', defendant_name);

drop index defendant_tsv_name;

UPDATE defendant set tsv_name = to_tsvector('simple', defendant_name);

CREATE INDEX defendant_tsv_name ON defendant USING gin(tsv_name);