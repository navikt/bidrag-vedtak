package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface VedtakRepository : JpaRepository<Vedtak, Int?>{

  @Query(
    "select st from Stonadsendring st where st.vedtak.vedtakId = :vedtakId")
  fun hentAlleStonadsendringerForVedtak(vedtakId: Int): List<Stonadsendring>
}
