package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.StønadsendringGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.StønadsendringGrunnlagPK
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface StønadsendringGrunnlagRepository : CrudRepository<StønadsendringGrunnlag, StønadsendringGrunnlagPK?> {

    @Query(
        "select sg from StønadsendringGrunnlag sg " +
            "where sg.stønadsendring.id = :stønadsendringId and sg.grunnlag.id = :grunnlagId"
    )
    fun hentStønadsendringGrunnlag(stønadsendringId: Int, grunnlagId: Int): StønadsendringGrunnlag

    @Query(
        "select sg from StønadsendringGrunnlag sg where sg.stønadsendring.id = :stønadsendringId order by sg.grunnlag.id"
    )
    fun hentAlleGrunnlagForStønadsendring(stønadsendringId: Int): List<StønadsendringGrunnlag>
}
