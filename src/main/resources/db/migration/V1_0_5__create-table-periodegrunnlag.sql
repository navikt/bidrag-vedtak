-- Table: periode

-- DROP TABLE periodegrunnlag;

CREATE TABLE IF NOT EXISTS periodegrunnlag
(
    periode_id integer NOT NULL,
    grunnlag_id integer NOT NULL,
    opprettet_av character(7),
    opprettet_timestamp timestamp DEFAULT now(),
    CONSTRAINT periodegrunnlag_pkey PRIMARY KEY (periode_id, grunnlag_id),
    CONSTRAINT fk_periode_id FOREIGN KEY (periode_id)
        REFERENCES periode (periode_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_grunnlag_id FOREIGN KEY (grunnlag_id)
        REFERENCES grunnlag (grunnlag_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

    TABLESPACE pg_default;