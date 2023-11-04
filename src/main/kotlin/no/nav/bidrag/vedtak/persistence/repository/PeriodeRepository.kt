package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Periode
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PeriodeRepository : CrudRepository<Periode, Int?> {

    @Query(
        "select pe from Periode pe where pe.stønadsendring.id = :stønadsendringsid order by pe.fom, pe.til"
    )
    fun hentAllePerioderForStønadsendring(stønadsendringsid: Int): List<Periode>
}
