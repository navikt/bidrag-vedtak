-- Table: engangsbeløpgrunnlag

-- DROP TABLE engangsbeløpgrunnlag;

CREATE TABLE IF NOT EXISTS engangsbeløpgrunnlag
(
    engangsbeløp_id integer NOT NULL,
    grunnlag_id integer NOT NULL,
    CONSTRAINT engangsbeløpgrunnlag_pkey PRIMARY KEY (engangsbeløp_id, grunnlag_id),
    CONSTRAINT engangsbeløpgrunnlag_id_fk FOREIGN KEY (engangsbeløp_id)
        REFERENCES engangsbeløp (engangsbeløp_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_grunnlag_id FOREIGN KEY (grunnlag_id)
        REFERENCES grunnlag (grunnlag_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (engangsbeløp_id, grunnlag_id)
)

    TABLESPACE pg_default;

CREATE INDEX idx_engangsbeløpgrunnlag_1 ON engangsbeløpgrunnlag(engangsbeløp_id);

CREATE INDEX idx_engangsbeløpgrunnlag_2 ON engangsbeløpgrunnlag(engangsbeløp_id, grunnlag_id);