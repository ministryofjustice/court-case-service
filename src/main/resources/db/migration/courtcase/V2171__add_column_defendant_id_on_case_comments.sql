BEGIN;

DROP INDEX case_comment_case_id_idx;

create table case_comments_bkp as table case_comments;
create index case_comments_bkp_case_id on case_comments_bkp(case_id);

truncate case_comments;
alter table case_comments add column defendant_id uuid not null;
create index case_comments_case_id_defendant_id on case_comments(case_id, defendant_id);


INSERT INTO case_comments (defendant_id,case_id,"comment",author,created,created_by,created_by_uuid,last_updated,last_updated_by,deleted,"version",is_draft)
select distinct hd.defendant_id, ccb.case_id, ccb."comment", ccb.author, ccb.created, ccb.created_by, ccb.created_by_uuid, ccb.last_updated,
                ccb.last_updated_by, ccb.deleted, ccb."version", ccb.is_draft from case_comments_bkp ccb
    join court_case cc on cc.case_id = ccb.case_id
    join hearing h on cc.id = h.fk_court_case_id
    join hearing_defendant hd on hd.fk_hearing_id = h.id
where cc.case_id = ccb.case_id;

ALTER TABLE case_comments ADD COLUMN LEGACY boolean NOT NULL DEFAULT FALSE;

UPDATE case_comments ccm SET legacy = true
    from case_comments ccm2
    	join (
                select hd.defendant_id as target_did from hearing_defendant hd join case_comments ccm3 on ccm3.defendant_id = hd.defendant_id
                    join (
                        select fk_hearing_id as fk_hearing_id_inner, count(hearing_defendant.id) from hearing_defendant
                            join case_comments on case_comments.defendant_id = hearing_defendant.defendant_id
                            group by fk_hearing_id having count(hearing_defendant.id) > 1
                    ) codef_hearing
                    on hd.fk_hearing_id = codef_hearing.fk_hearing_id_inner
    	    ) codefs
on ccm2.DEFENDANT_ID = codefs.target_did
where ccm.defendant_id = codefs.target_did;

COMMIT;