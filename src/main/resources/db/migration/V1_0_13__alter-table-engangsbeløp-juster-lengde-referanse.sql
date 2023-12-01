DROP INDEX idx_engangsbeløp_2;

ALTER TABLE engangsbeløp
ALTER COLUMN referanse TYPE text ;

ALTER TABLE engangsbeløp
    ALTER COLUMN referanse SET DEFAULT '';

ALTER TABLE engangsbeløp
    ALTER COLUMN referanse SET NOT NULL;