BEGIN;

-- Supports case-insensitive lookups like `upper(cc.urn) = upper(:urn)` used by
-- DefendantRepositoryCustom#findDefendantsByUrn. Without this, Postgres has to
-- sequentially scan the entire court_case table to evaluate upper(urn).
CREATE INDEX IF NOT EXISTS court_case_urn_upper_idx ON court_case (upper(urn));

COMMIT;
