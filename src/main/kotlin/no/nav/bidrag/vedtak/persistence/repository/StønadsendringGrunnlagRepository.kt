package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.StønadsendringGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.StønadsendringGrunnlagPK
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface StønadsendringGrunnlagRepository : CrudRepository<StønadsendringGrunnlag, StønadsendringGrunnlagPK?> {

    @Query(
        "select sg from StønadsendringGrunnlag sg " +
            "where sg.stønadsendring.id = :stønadsendringsid and sg.grunnlag.id = :grunnlagsid",
    )
    fun hentStønadsendringGrunnlag(stønadsendringsid: Int, grunnlagsid: Int): StønadsendringGrunnlag

    @Query(
        "select sg from StønadsendringGrunnlag sg where sg.stønadsendring.id = :stønadsendringsid order by sg.grunnlag.id",
    )
    fun hentAlleGrunnlagForStønadsendring(stønadsendringsid: Int): List<StønadsendringGrunnlag>

    @Modifying
    @Transactional
    @Query(
        "delete from StønadsendringGrunnlag sg where sg.stønadsendring.id = :stønadsendringsid",
    )
    fun slettStønadsendringGrunnlagForStønadsendring(@Param("stønadsendringsid") stønadsendringsid: Int): Int
}
