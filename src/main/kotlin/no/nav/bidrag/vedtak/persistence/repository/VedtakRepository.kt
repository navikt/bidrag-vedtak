package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface VedtakRepository : JpaRepository<Vedtak, Int?> {

    @Query(
        "select v from Vedtak v where v.unikReferanse = :unikReferanse",
    )
    fun hentVedtakForUnikReferanse(unikReferanse: String): Vedtak
}
