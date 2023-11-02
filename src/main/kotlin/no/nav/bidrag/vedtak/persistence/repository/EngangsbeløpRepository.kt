package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Engangsbeløp
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface EngangsbeløpRepository : CrudRepository<Engangsbeløp, Int?> {

    @Query(
        "select eb from Engangsbeløp eb where eb.vedtak.id = :vedtakId order by eb.id"
    )
    fun hentAlleEngangsbeløpForVedtak(vedtakId: Int): List<Engangsbeløp>
}
