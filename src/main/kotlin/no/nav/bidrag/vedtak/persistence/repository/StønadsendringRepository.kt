package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Stønadsendring
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface StønadsendringRepository : CrudRepository<Stønadsendring, Int?> {

    @Query(
        "select st from Stønadsendring st where st.vedtak.id = :vedtaksid order by st.id",
    )
    fun hentAlleStønadsendringerForVedtak(vedtaksid: Int): List<Stønadsendring>

    @Query(
        "select st from Stønadsendring st where st.sak = :saksnr and st.type = :type and st.skyldner = :skyldner and st.kravhaver = :kravhaver " +
            "and st.vedtak.vedtakstidspunkt is not null order by st.vedtak.id",
    )
    fun hentVedtakForStønad(saksnr: String, type: String, skyldner: String, kravhaver: String): List<Stønadsendring>

    // Sletter angitt stønadsendring
    @Modifying
    @Query(
        "delete from Stønadsendring se where se.id = :stønadsendringsid",
    )
    fun slettStønadsendring(stønadsendringsid: Int): Int
}
