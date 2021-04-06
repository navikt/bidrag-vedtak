-- Table: periode

-- DROP TABLE grunnlag;

CREATE TABLE IF NOT EXISTS grunnlag
(
    grunnlag_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    grunnlag_referanse varchar(50) NOT NULL,
    vedtak_id integer NOT NULL,
    grunnlag_type varchar(20) NOT NULL,
    grunnlag_innhold jsonb NOT NULL,
    CONSTRAINT grunnlag_pkey PRIMARY KEY (grunnlag_id),
    CONSTRAINT fk_vedtak_id FOREIGN KEY (vedtak_id)
        REFERENCES vedtak (vedtak_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (vedtak_id, grunnlag_referanse)
)

    TABLESPACE pg_default;