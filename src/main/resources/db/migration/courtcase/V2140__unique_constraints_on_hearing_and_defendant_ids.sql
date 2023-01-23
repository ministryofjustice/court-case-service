BEGIN;

    -- The goal of this migration is to clear down some duplicate hearings and defendants. To do this it also needs to
    -- tidy up any null references to deleted records so that the correct foreign key constraints can be reintroduced at
    -- the end.

    -- Firstly drop constraints which will block deletion of the duplicates.
    ALTER TABLE hearing_defendant DROP CONSTRAINT FK_HEARING_DEFENDANT_DEFENDANT;
    ALTER TABLE hearing_defendant DROP CONSTRAINT FK_HEARING_DEFENDANT_HEARING;

    -- A HEARING_DEFENDANT cannot exist without its corresponding hearing, so we can immediately re-add the foreign
    -- key constraint to HEARING with the CASCADE option and when that HEARING is deleted the HEARING_DEFENDANT will be
    -- deleted with it, leaving no null references.
    ALTER TABLE HEARING_DEFENDANT ADD CONSTRAINT FK_HEARING_DEFENDANT_HEARING FOREIGN KEY (FK_HEARING_ID) REFERENCES HEARING ON DELETE CASCADE;

    -- We cannot do this for DEFENDANT because a DEFENDANT can be referenced by multiple HEARING_DEFENDANTS, we will
    -- have to clear these up later

    -- Delete duplicate hearings and defendants, keeping the most recent record
    DELETE FROM hearing h USING (select id from hearing h2 except SELECT max(id) FROM hearing h2 GROUP BY  h2.hearing_id) as to_delete where h.id = to_delete.id;
    DELETE FROM defendant d USING (select id from defendant d2 except SELECT max(id) FROM defendant d2 GROUP BY d2.defendant_id) as to_delete where d.id = to_delete.id;

    -- Update each null DEFENDANT foreign key reference in HEARING_DEFENDANT with the ID of the remaining DEFENDANT with
    -- a matching DEFENDANT_ID
    UPDATE HEARING_DEFENDANT hd SET FK_DEFENDANT_ID = d.id
    from hearing_defendant hd2
    	join defendant d on hd2.defendant_id = d.defendant_id
    	left outer join defendant d2 ON hd2.fk_defendant_id = d2.id
    WHERE hd.DEFENDANT_ID = d.DEFENDANT_ID and d2.id is null;

    -- Reintroduce the foreign key constraint on FK_DEFENDANT_ID now that we have no more null references
    ALTER TABLE HEARING_DEFENDANT ADD CONSTRAINT FK_HEARING_DEFENDANT_DEFENDANT FOREIGN KEY (FK_DEFENDANT_ID) REFERENCES DEFENDANT;

    -- Add unique constraints on DEFENDANT_ID and HEARING_ID to make sure we don't get duplicates again
    ALTER TABLE hearing ADD CONSTRAINT hearing_id_unique_key UNIQUE (hearing_id);
    ALTER TABLE defendant ADD CONSTRAINT defendant_id_unique_key UNIQUE (defendant_id);

COMMIT;
