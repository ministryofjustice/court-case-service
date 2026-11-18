BEGIN;

-- Supports case-insensitive lookups like `upper(d.pnc) = upper(:pnc)` used by
-- DefendantRepositoryCustom#findDefendantsByPnc. The existing `defendant_names`
-- index is on raw `pnc` and cannot be used for an `upper(pnc)` predicate.
CREATE INDEX IF NOT EXISTS defendant_pnc_upper_idx ON defendant (upper(pnc));

COMMIT;
