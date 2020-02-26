INSERT INTO court (id, name, court_code) VALUES (1142407, 'Sheffield Magistrates Court', 'SHF');
INSERT INTO court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, data, last_updated, previously_known_termination_date, suspended_sentence_order, breach) VALUES (1168460, 1600028912, 'SHF', 1, '2019-12-14 09:00', 'No record', '{"inf": "POL01", "h_id": 1246272, "def_name": "JCONE", "def_addr": {"line3": "a3", "line2": "a2", "line1": "a1"}, "type": "C", "caseno": 1600028912, "def_dob": "01/01/1998", "valid": "Y", "cseq": 1, "listno": "1st", "offences": {"offence": [{"as": "Contrary to section 1(1) and 7 of the Theft Act 1968.", "code": "TH68010", "oseq": 1, "co_id": 1142407, "maxpen": "EW: 6M &/or Ultd Fine", "sum": "On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.", "title": "Theft from a shop"}, {"as": "Contrary to section 1(1) and 7 of the Theft Act 1968.", "code": "TH68010", "oseq": 2, "co_id": 1142408, "maxpen": "EW: 6M &/or Ultd Fine", "sum": "On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.", "title": "Theft from a shop"}]}, "def_age": 18, "c_id": 1168460, "def_type": "P", "def_sex": "M"}', '2019-12-14 09:00', null, false, false);
INSERT INTO court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, data, last_updated, previously_known_termination_date, suspended_sentence_order, breach) VALUES (1246257, 1600028974, 'SHF', 1, '2020-01-13 09:00', 'Current', '{"inf":"POL01","h_id":1246257,"def_name":"Mr Dylan Adam Armstrong","def_addr":{"pcode":"S1 6ua","line3":"line3","line2":"line2","line1":"line1"},"type":"C","caseno":1600028974,"def_dob":"02/08/1990","valid":"Y","cseq":1,"listno":"1st","offences":{"offence":{"as":"Contrary to section 363(2) and (4) of the Communications Act 2003.","code":"CA03010","oseq":1,"co_id":1142431,"maxpen":"S: L3","sum":"On 05/09/2016 used a colour television receiver without a licence at the above address.","sof":".","title":"Use a television set without a licence"}},"def_age":29,"c_id":1168463,"def_type":"P","def_sex":"N"}', '2019-12-14 09:00', null, true, true);
INSERT INTO court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, data, last_updated, previously_known_termination_date, suspended_sentence_order, breach) VALUES (1246258, 1600028956, 'SHF', 1, '2020-01-13 09:00', 'Previously known', '{"inf":"POL01","h_id":1246258,"def_name":"Mr Joe JMBBLOGGS","def_addr":{"pcode":"S1 6UA","line3":"l3","line2":"line2","line1":"line1"},"type":"C","caseno":1600028956,"def_dob":"16/08/1990","valid":"Y","cseq":2,"listno":"1st","offences":{"offence":{"as":"Contrary to section 363(2) and (4) of the Communications Act 2003.","code":"CA03010","oseq":1,"co_id":1142414,"maxpen":"S: L3","sum":"On 05/09/2016 used a colour television receiver without a licence at the above address.","sof":".","title":"Use a television set without a licence"}},"def_age":29,"c_id":1168464,"def_type":"P","def_sex":"N"}', '2019-12-14 09:00', '2000-01-01', true, true);
INSERT INTO court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, data, last_updated, previously_known_termination_date, suspended_sentence_order, breach) VALUES (1246273, 1600028920, 'SHF', 1, '2020-01-13 09:00', 'No record', '{"inf":6543,"h_id":1246273,"def_name":"MR TEST OLLIEONE","def_addr":{"line4":"THE PLACE","pcode":"POST COD","line3":"THE TOWN","line2":"THE STREET","line1":"THE HOUSE"},"type":"C","caseno":1600028920,"def_dob":"08/08/1933","valid":"Y","cseq":1,"listno":"1st","offences":{"offence":[{"code":"TA02002","oseq":1,"co_id":1142409,"sum":"THIS IS THE OFFENCE WORDING","title":"Caused to be published a tobacco advertisement"},{"as":"Contrary to section 1(1) and 7 of the Theft Act 1968.","code":"TH68001","oseq":2,"co_id":1142410,"maxpen":"EW: 6M &/or Ultd Fine","sum":"On 24/03/2016 at BURY ST EDS in the county of SUFFOLK stole XBOX ONE, to the value of 300.00, belonging to MR GAMESMASTER.","title":"Theft from the person of another"}]},"def_age":83,"c_id":1168461,"def_type":"P","def_sex":"M"}', '2019-12-14 09:00', null, false, false);
INSERT INTO court_case (case_id, case_no, court_code, court_room, session_start_time, probation_status, data, last_updated, previously_known_termination_date, suspended_sentence_order, breach) VALUES (1248278, 1600029021, 'SHF', 1, '2020-01-13 09:00', 'No record', '{"inf":"POL01","h_id":1248278,"def_name":"Mr Ureet JMBALERNAEU","def_addr":{"pcode":"rg2 6ua","line3":"line3","line2":"line2","line1":"line1"},"type":"C","caseno":1600029021,"def_dob":"02/08/1990","valid":"Y","cseq":1,"listno":"1st","offences":{"offence":{"as":"Contrary to section 363(2) and (4) of the Communications Act 2003.","code":"CA03010","oseq":1,"co_id":1144413,"maxpen":"S: L3","sum":"On 06/09/2016 used a colour television receiver without a licence at the above address.","sof":".","title":"Use a television set without a licence"}},"def_age":26,"c_id":1170464,"def_type":"P","def_sex":"N"}', '2019-12-14 09:00', null, false, false);


INSERT INTO OFFENCE (
	CASE_ID,
	OFFENCE_TITLE,
    OFFENCE_SUMMARY,
    ACT,
	SEQUENCE_NUMBER
	) VALUES (
        1168460,
        'Theft from a shop',
        'On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.',
        'Contrary to section 1(1) and 7 of the Theft Act 1968.',
        1
	);

INSERT INTO OFFENCE (
	CASE_ID,
	OFFENCE_TITLE,
    OFFENCE_SUMMARY,
    ACT,
	SEQUENCE_NUMBER
	) VALUES (
        1168460,
        'Theft from a different shop',
        'On 02/01/2015 at own, stole article, to the value of £987.00, belonging to person.',
        'Contrary to section 1(1) and 7 of the Theft Act 1968.',
        2
	);

