-- Table: stønadsendringgrunnlag

-- DROP TABLE stønadsendringgrunnlag;

CREATE TABLE IF NOT EXISTS stønadsendringgrunnlag
(
    stønadsendringid integer NOT NULL,
    grunnlagsid integer NOT NULL,
    CONSTRAINT stønadsendringgrunnlag_pkey PRIMARY KEY (stønadsendringid, grunnlagsid),
    CONSTRAINT fk_stønadsendring_id FOREIGN KEY (stønadsendringid)
        REFERENCES stønadsendring (stønadsendringid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_grunnlag_id FOREIGN KEY (grunnlagsid)
        REFERENCES grunnlag (grunnlagsid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (stønadsendringid, grunnlagsid)
)

    TABLESPACE pg_default;

CREATE INDEX idx_stønadsendringgrunnlag_1 ON stønadsendringgrunnlag(stønadsendringid);

CREATE INDEX idx_stønadsendringgrunnlag_2 ON stønadsendringgrunnlag(stønadsendringid, grunnlagsid);