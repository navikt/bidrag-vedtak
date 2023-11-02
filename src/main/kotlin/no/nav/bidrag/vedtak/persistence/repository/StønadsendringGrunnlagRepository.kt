package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.StønadsendringGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.StønadsendringGrunnlagPK
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface StønadsendringGrunnlagRepository : CrudRepository<StønadsendringGrunnlag, StønadsendringGrunnlagPK?> {

    @Query(
        "select pg from StønadsendringGrunnlag pg " +
            "where pg.stønadsendring.id = :stønadsendringId and pg.grunnlag.id = :grunnlagId"
    )
    fun hentStønadsendringGrunnlag(stønadsendringId: Int, grunnlagId: Int): StønadsendringGrunnlag

    @Query(
        "select pg from StønadsendringGrunnlag pg where pg.stønadsendring.id = :stønadsendringId order by pg.grunnlag.id"
    )
    fun hentAlleGrunnlagForStønadsendring(stønadsendringId: Int): List<StønadsendringGrunnlag>
}
