-- This migration undoes most of the effects (with the exception of adding the SHF court as this is needed) of
-- V99__seed_data.sql which was inadvertently applied to prod.

DELETE FROM court_case WHERE case_id='1168460';
DELETE FROM court_case WHERE case_id='1246257';
DELETE FROM court_case WHERE case_id='1246258';
DELETE FROM court_case WHERE case_id='1246273';
DELETE FROM court_case WHERE case_id='1248278';

