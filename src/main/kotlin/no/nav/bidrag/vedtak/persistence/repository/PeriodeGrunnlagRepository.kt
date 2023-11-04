package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.PeriodeGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.PeriodeGrunnlagPK
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PeriodeGrunnlagRepository : CrudRepository<PeriodeGrunnlag, PeriodeGrunnlagPK?> {

    @Query(
        "select pg from PeriodeGrunnlag pg " +
            "where pg.periode.id = :periodeid and pg.grunnlag.id = :grunnlagsid"
    )
    fun hentPeriodeGrunnlag(periodeid: Int, grunnlagsid: Int): PeriodeGrunnlag

    @Query(
        "select pg from PeriodeGrunnlag pg where pg.periode.id = :periodeid order by pg.grunnlag.id"
    )
    fun hentAlleGrunnlagForPeriode(periodeid: Int): List<PeriodeGrunnlag>
}
