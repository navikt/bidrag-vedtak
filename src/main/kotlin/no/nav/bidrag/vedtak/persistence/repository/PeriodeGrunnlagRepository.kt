package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.PeriodeGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.PeriodeGrunnlagPK
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PeriodeGrunnlagRepository : CrudRepository<PeriodeGrunnlag, PeriodeGrunnlagPK?>{

  @Query(
    "select pg from PeriodeGrunnlag pg " +
        "where pg.periode.periodeId = :periodeId and pg.grunnlag.grunnlagId = :grunnlagId"
  )
  fun hentPeriodeGrunnlag(periodeId: Int, grunnlagId: Int): PeriodeGrunnlag

  @Query(
    "select pg from PeriodeGrunnlag pg where pg.periode.periodeId = :periodeId"
  )
  fun hentAlleGrunnlagForPeriode(periodeId: Int): List<PeriodeGrunnlag>

}
