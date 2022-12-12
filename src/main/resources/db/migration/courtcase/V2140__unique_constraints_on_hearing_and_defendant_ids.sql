BEGIN;

    DELETE FROM hearing h USING (select id from hearing h2 except SELECT max(id) FROM hearing h2 GROUP BY  h2.hearing_id) as to_delete where h.id = to_delete.id;
    DELETE FROM defendant d USING (select id from defendant d2 except SELECT max(id) FROM defendant d2 GROUP BY d2.defendant_id) as to_delete where d.id = to_delete.id;

    ALTER TABLE hearing ADD CONSTRAINT hearing_id_unique_key UNIQUE (hearing_id);
    ALTER TABLE defendant ADD CONSTRAINT defendant_id_unique_key UNIQUE (defendant_id);

COMMIT