package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Stønadsendring
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface StønadsendringRepository : CrudRepository<Stønadsendring, Int?> {

    @Query(
        "select st from Stønadsendring st where st.vedtak.id = :vedtakId order by st.id"
    )
    fun hentAlleStønadsendringerForVedtak(vedtakId: Int): List<Stønadsendring>
}
