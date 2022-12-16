-- Table: engangsbelopgrunnlag

-- DROP TABLE engangsbelopgrunnlag;

CREATE TABLE IF NOT EXISTS engangsbelopgrunnlag
(
    engangsbelop_id integer NOT NULL,
    grunnlag_id integer NOT NULL,
    CONSTRAINT engangsbelopgrunnlag_pkey PRIMARY KEY (engangsbelop_id, grunnlag_id),
    CONSTRAINT engangsbelopgrunnlag_id_fk FOREIGN KEY (engangsbelop_id)
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

CREATE INDEX idx_engangsbelopgrunnlag_1 ON engangsbelopgrunnlag(engangsbelop_id);

CREATE INDEX idx_engangsbelopgrunnlag_2 ON engangsbelopgrunnlag(engangsbelop_id, grunnlag_id);