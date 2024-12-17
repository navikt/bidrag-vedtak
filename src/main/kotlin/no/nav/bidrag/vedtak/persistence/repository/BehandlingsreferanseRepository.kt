package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Behandlingsreferanse
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface BehandlingsreferanseRepository : CrudRepository<Behandlingsreferanse, Int?> {

    @Query(
        "select be from Behandlingsreferanse be where be.vedtak.id = :vedtaksid order by be.id",
    )
    fun hentAlleBehandlingsreferanserForVedtak(vedtaksid: Int): List<Behandlingsreferanse>

    @Query(
        "select br.vedtak.id from Behandlingsreferanse br where br.kilde = :kilde and br.referanse = :behandlingsreferanse",
    )
    fun hentVedtaksidForBehandlingsreferanse(kilde: String, behandlingsreferanse: String): Int?
}
