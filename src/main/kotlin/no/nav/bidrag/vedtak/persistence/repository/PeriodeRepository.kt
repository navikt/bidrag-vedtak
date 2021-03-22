package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Periode
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PeriodeRepository : CrudRepository<Periode, Int?>{

  @Query(
    "select pe from Periode pe where pe.stonadsendring.stonadsendringId = :stonadsendringsId")
  fun hentAllePerioderForStonadsendringId(stonadsendringsId: Int): List<Periode>
}
