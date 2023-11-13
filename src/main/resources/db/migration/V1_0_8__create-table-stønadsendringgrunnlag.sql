-- Table: stønadsendringgrunnlag

-- DROP TABLE stønadsendringgrunnlag;

CREATE TABLE IF NOT EXISTS stønadsendringgrunnlag
(
    stønadsendringsid integer NOT NULL,
    grunnlagsid integer NOT NULL,
    CONSTRAINT stønadsendringgrunnlag_pkey PRIMARY KEY (stønadsendringsid, grunnlagsid),
    CONSTRAINT fk_stønadsendring_id FOREIGN KEY (stønadsendringsid)
        REFERENCES stønadsendring (stønadsendringsid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_grunnlag_id FOREIGN KEY (grunnlagsid)
        REFERENCES grunnlag (grunnlagsid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (stønadsendringsid, grunnlagsid)
)

    TABLESPACE pg_default;

CREATE INDEX idx_stønadsendringgrunnlag_1 ON stønadsendringgrunnlag(stønadsendringsid);

CREATE INDEX idx_stønadsendringgrunnlag_2 ON stønadsendringgrunnlag(stønadsendringsid, grunnlagsid);