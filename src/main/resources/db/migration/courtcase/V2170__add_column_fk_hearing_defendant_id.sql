BEGIN;



ALTER TABLE HEARING_NOTES ADD COLUMN FK_HEARING_DEFENDANT_ID int4;

ALTER TABLE HEARING_NOTES ADD COLUMN LEGACY boolean NOT NULL DEFAULT FALSE;

CREATE INDEX IDX_HEARING_NOTES_FK_HEARING_DEFENDANT_ID on HEARING_NOTES(FK_HEARING_DEFENDANT_ID);

ALTER TABLE HEARING_NOTES ADD CONSTRAINT fk_hearing_note_hearing_defendant FOREIGN KEY (fk_hearing_defendant_id) REFERENCES hearing_defendant(id) ON DELETE CASCADE;

-- scripts to copy over existing notes to co-defendants

create table hearing_notes_bkp as table hearing_notes;

ALTER TABLE HEARING_NOTES ADD COLUMN LEGACY boolean NOT NULL DEFAULT FALSE;

truncate table hearing_notes;

insert into hearing_notes
(FK_HEARING_DEFENDANT_ID, hearing_id, note, author, created, created_by, created_by_uuid, last_updated, last_updated_by, deleted, "version", draft, legacy)
select hd.id as FK_HEARING_DEFENDANT_ID, hn.hearing_id, hn.note, hn.author, hn.created, hn.created_by, hn.created_by_uuid,
       hn.last_updated, hn.last_updated_by, hn.deleted, hn."version", hn.draft, true as legacy
from hearing h join hearing_notes_bkp hn on hn.hearing_id = h.hearing_id
               join hearing_defendant hd on hd.fk_hearing_id = h.id

COMMIT;