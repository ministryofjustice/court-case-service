BEGIN;
    ALTER TABLE judicial_result DROP CONSTRAINT fk_offence;
    ALTER TABLE judicial_result ADD CONSTRAINT fk_offence FOREIGN KEY (offence_id) REFERENCES offence (id) ON DELETE CASCADE;

    DELETE FROM hearing h USING (select id from hearing h2 except SELECT max(id) FROM hearing h2 GROUP BY  h2.hearing_id) as to_delete where h.id = to_delete.id;

COMMIT