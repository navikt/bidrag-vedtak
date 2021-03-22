package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface StonadsendringRepository : CrudRepository<Stonadsendring, Int?>{

  @Query(
    "select st from Stonadsendring st where st.vedtak.vedtakId = :vedtakId")
  fun hentAlleStonadsendringerForVedtak(vedtakId: Int): List<Stonadsendring>
}
