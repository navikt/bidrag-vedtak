-- Table: grunnlag

-- DROP TABLE grunnlag;

CREATE TABLE IF NOT EXISTS grunnlag
(
    grunnlag_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    referanse varchar(100) NOT NULL,
    vedtak_id integer NOT NULL,
    type varchar(50) NOT NULL,
    innhold text NOT NULL,
    CONSTRAINT grunnlag_pkey PRIMARY KEY (grunnlag_id),
    CONSTRAINT fk_vedtak_id FOREIGN KEY (vedtak_id)
        REFERENCES vedtak (vedtak_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (vedtak_id, referanse)
)

    TABLESPACE pg_default;

CREATE INDEX idx_grunnlag_1 ON grunnlag(vedtak_id);