BEGIN;

create table hearing_notes_bkp as table hearing_notes;

truncate table hearing_notes;

ALTER TABLE hearing_notes ADD COLUMN FK_HEARING_DEFENDANT_ID int4;

ALTER TABLE hearing_notes ADD CONSTRAINT fk_hearing_note_hearing_defendant FOREIGN KEY (fk_hearing_defendant_id) REFERENCES hearing_defendant(id) ON DELETE CASCADE;

CREATE INDEX IDX_HEARING_NOTES_FK_HEARING_DEFENDANT_ID on hearing_notes(FK_HEARING_DEFENDANT_ID);

insert into hearing_notes
(FK_HEARING_DEFENDANT_ID, hearing_id, note, author, created, created_by, created_by_uuid, last_updated, last_updated_by, deleted, "version", draft)
select hd.id as FK_HEARING_DEFENDANT_ID, hn.hearing_id, hn.note, hn.author, hn.created, hn.created_by, hn.created_by_uuid,
       hn.last_updated, hn.last_updated_by, hn.deleted, hn."version", hn.draft
from hearing h join hearing_notes_bkp hn on hn.hearing_id = h.hearing_id
               join hearing_defendant hd on hd.fk_hearing_id = h.id;

-- scripts to set legacy flag on notes for co-defendants
ALTER TABLE hearing_notes ADD COLUMN LEGACY boolean NOT NULL DEFAULT FALSE;

UPDATE hearing_notes hn SET legacy = true
    from hearing_notes hn2
    	join (
                select hd.id as target_hdid from hearing_defendant hd join hearing_notes hn3 on hn3.fk_hearing_defendant_id = hd.id
                    join (
                        select fk_hearing_id as fk_hearing_id_inner, count(hearing_defendant.id) from hearing_defendant
							join hearing_notes on hearing_notes.fk_hearing_defendant_id = hearing_defendant.id
                        group by fk_hearing_id having count(hearing_defendant.id) > 1
                    ) codef_hearing
                    on hd.fk_hearing_id = codef_hearing.fk_hearing_id_inner
    	    ) codefs
on hn2.FK_HEARING_DEFENDANT_ID = codefs.target_hdid
where hn.FK_HEARING_DEFENDANT_ID = codefs.target_hdid;

COMMIT;