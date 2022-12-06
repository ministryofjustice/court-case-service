BEGIN;

ALTER TABLE judicial_result DROP CONSTRAINT fk_offence;
ALTER TABLE judicial_result ADD CONSTRAINT fk_offence FOREIGN KEY (offence_id) REFERENCES offence (id) ON DELETE CASCADE;

DELETE FROM hearing h WHERE h.id  NOT IN (SELECT max(id) FROM hearing h2 GROUP BY  h2.hearing_id);

DELETE FROM defendant d WHERE d.defendant_id  NOT IN (SELECT hd.defendant_id FROM hearing_defendant hd);

COMMIT;