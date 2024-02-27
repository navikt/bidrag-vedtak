alter table grunnlag
    alter column referanse type text using referanse::text;

alter table grunnlag
    alter column type type text using type::text;

