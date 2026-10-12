BEGIN;

-- offence.act was converted from varchar(255) to text in V2165, but the corresponding
-- Envers audit table (offence_aud) was never updated, causing
-- "value too long for type character varying(255)" errors when a long act/statute
-- citation is audited.
ALTER TABLE IF EXISTS courtcaseservicetest.offence_aud ALTER COLUMN act TYPE TEXT;

COMMIT;
