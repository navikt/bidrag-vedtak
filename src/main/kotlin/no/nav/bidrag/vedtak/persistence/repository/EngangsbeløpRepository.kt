package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Engangsbeløp
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface EngangsbeløpRepository : CrudRepository<Engangsbeløp, Int?> {

    @Query(
        "select eb from Engangsbeløp eb where eb.vedtak.id = :vedtaksid order by eb.id"
    )
    fun hentAlleEngangsbeløpForVedtak(vedtaksid: Int): List<Engangsbeløp>

    @Query(
        "select eb.referanse from Engangsbeløp eb where eb.vedtak.id = :vedtaksid and eb.referanse = :referanse"
    )
    fun sjekkReferanse(vedtaksid: Int, referanse: String): String?
}
