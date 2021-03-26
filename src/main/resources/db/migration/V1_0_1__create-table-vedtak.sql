-- Table: vedtak

-- DROP TABLE vedtak;

CREATE TABLE IF NOT EXISTS vedtak
(
    vedtak_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    enhet_id character(4),
    opprettet_av character(7),
    opprettet_timestamp timestamp DEFAULT now(),
    CONSTRAINT vedtak_pkey PRIMARY KEY (vedtak_id)
)

    TABLESPACE pg_default;