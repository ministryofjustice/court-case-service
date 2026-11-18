BEGIN;

-- Supports case-insensitive lookups like `upper(off.crn) = upper(:crn)` used by
-- DefendantRepositoryCustom#findDefendantsByCrn. Without this, Postgres has to
-- sequentially scan the entire offender table to evaluate upper(crn).
CREATE INDEX IF NOT EXISTS offender_crn_upper_idx ON offender (upper(crn));

COMMIT;
