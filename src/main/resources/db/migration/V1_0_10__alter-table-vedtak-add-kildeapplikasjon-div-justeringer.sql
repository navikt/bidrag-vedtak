ALTER TABLE vedtak
ADD COLUMN kildeapplikasjon TYPE varchar(255) NOT NULL;

ALTER TABLE vedtak
ALTER COLUMN opprettet_av TYPE varchar(255) NOT NULL;

ALTER TABLE vedtak
ALTER COLUMN enhetsnummer TYPE character(4);