
-- adhoc SQL for individual dates (5th and 6th August) 
select *
from courtcaseservice.hearing h join courtcaseservice.hearing_defendant hdef on (hdef.fk_hearing_id = h.id)
join courtcaseservice.hearing_day hd on (hd.fk_hearing_id = h.id)
join courtcaseservice.offence o on (hdef.id = o.fk_hearing_defendant_id)
join courtcaseservice.defendant d on (hdef.defendant_id = d.defendant_id)
where lower(h.hearing_type) in ('bail application', 'bail variation application')
and date(hd.hearing_day) = DATE('2026-08-06');

-- THIS SQL IS TO BE RUN ON A DAILY BASIS TO PROVIDE A LIST OF BAIL APPLICATIONS AND BAIL VARIATION APPLICATIONS FOR THE NEXT 14 DAYS.
select 	h.id,
		h.hearing_id,
		h.hearing_event_type,
		h.hearing_type,
		hd.hearing_day,
		hd.hearing_time,
		o.summary,
		o.title,
		o."sequence",
		o.act,
		o.offence_code,
		o.plea_id,
		o.verdict_id,
		d.defendant_name,
		d."type",
		d."name",
		d.address,
		d.crn,
		d.pnc,
		d.cro,
		d.date_of_birth,
		d.sex,
		d.nationality_1,
		d.nationality_2,
		cc.urn,
		c."name",
		c.court_code
from courtcaseservice.hearing h
join courtcaseservice.hearing_defendant hdef on hdef.fk_hearing_id = h.id
join courtcaseservice.hearing_day hd on hd.fk_hearing_id = h.id
join courtcaseservice.offence o on hdef.id = o.fk_hearing_defendant_id
join courtcaseservice.defendant d on hdef.defendant_id = d.defendant_id
join courtcaseservice.court_case cc on h.fk_court_case_id = cc.id
join courtcaseservice.court c on c.court_code = hd.court_code
where lower(h.hearing_type) in ('bail application', 'bail variation application')
and hd.hearing_day::date between current_date + 1 and current_date + 14
order by hd.hearing_day asc;
