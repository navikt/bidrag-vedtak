package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface VedtakRepository : JpaRepository<Vedtak, Int?> {

    @Query(
        "select v.id from Vedtak v where v.vedtakstidspunkt is null",
    )
    fun hentAlleVedtaksforslagIder(): List<Int>

    @Query(
        "select v from Vedtak v where v.unikReferanse = :unikReferanse",
    )
    fun hentVedtakForUnikReferanse(unikReferanse: String): Vedtak?

    // Slett vedtak
    @Modifying
    @Query(
        "delete from Vedtak ve where ve.id = :vedtaksid",
    )
    fun slettVedtak(vedtaksid: Int): Int
}
