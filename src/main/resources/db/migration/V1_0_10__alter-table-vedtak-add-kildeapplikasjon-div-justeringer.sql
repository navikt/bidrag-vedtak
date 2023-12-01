ALTER TABLE vedtak
ADD COLUMN kildeapplikasjon text NOT NULL default '';

ALTER TABLE vedtak
ALTER COLUMN opprettet_av TYPE text;

ALTER TABLE vedtak
ALTER COLUMN enhetsnummer TYPE text;