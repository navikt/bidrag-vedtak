ALTER TABLE vedtak
    ALTER COLUMN vedtak_type TYPE varchar(50);
ALTER TABLE vedtak
    ALTER COLUMN vedtak_type SET default 'MANUELT' ;