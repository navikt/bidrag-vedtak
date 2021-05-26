-- Table: engangsgrunnlag

-- DROP TABLE engangsgrunnlag;

CREATE TABLE IF NOT EXISTS engangsbelopgrunnlag
(
    engangsbelop_id integer NOT NULL,
    grunnlag_id integer NOT NULL,
    CONSTRAINT periodegrunnlag_pkey PRIMARY KEY (engangsbelop_id, grunnlag_id),
    CONSTRAINT fk_periode_id FOREIGN KEY (engangsbelop_id)
        REFERENCES engangsbelop (engangsbelop_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_grunnlag_id FOREIGN KEY (grunnlag_id)
        REFERENCES grunnlag (grunnlag_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (engangsbelop_id, grunnlag_id)
)

    TABLESPACE pg_default;