BEGIN;

create table hearing_outcome_bkp as table hearing_outcome;

CREATE INDEX  hearing_outcome_bkp_fk_hearing_id_idx on hearing_outcome (fk_hearing_id);

truncate hearing_outcome;

drop index if exists hearing_outcome_fk_hearing_id_idx;

ALTER TABLE  hearing_outcome DROP CONSTRAINT if exists hearing_outcome_unique_fk_hearing_id;

ALTER TABLE  hearing_outcome DROP CONSTRAINT if exists fk_hearing_hearing_outcome;

ALTER TABLE hearing_outcome ADD COLUMN fk_hearing_defendant_id integer;
ALTER TABLE hearing_outcome ADD COLUMN LEGACY boolean NOT NULL DEFAULT FALSE;

CREATE INDEX hearing_outcome_fk_hearing_defendant_id_idx on hearing_outcome (fk_hearing_defendant_id);

ALTER TABLE  hearing_outcome ADD CONSTRAINT fk_hearing_defendant_hearing_outcome FOREIGN KEY (fk_hearing_defendant_id) REFERENCES hearing_defendant;

INSERT INTO hearing_outcome (fk_hearing_defendant_id, fk_hearing_id, outcome_type,created,created_by,deleted, last_updated,
                                 last_updated_by,"version",outcome_date,state,assigned_to,assigned_to_uuid,resulted_date)
select hd.id, ho.fk_hearing_id, ho.outcome_type, ho.created, ho.created_by, ho.deleted, ho.last_updated,
       ho.last_updated_by, ho."version", ho.outcome_date, ho.state, ho.assigned_to, ho.assigned_to_uuid, ho.resulted_date
from hearing_outcome_bkp ho
         join hearing_defendant hd on hd.fk_hearing_id = ho.fk_hearing_id;

UPDATE hearing_outcome hot SET legacy = true
    from hearing_outcome hot2
    	join (
            select hearing_defendant.fk_hearing_id as fk_hearing_id_inner, count(hearing_defendant.id) from hearing_defendant
                join hearing_outcome on hearing_outcome.fk_hearing_id = hearing_defendant.fk_hearing_id
                group by hearing_defendant.fk_hearing_id having count(hearing_defendant.id) > 1
        ) codef_hearing on codef_hearing.fk_hearing_id_inner = hot2.fk_hearing_id
where hot.fk_hearing_id = hot2.fk_hearing_id;

END;