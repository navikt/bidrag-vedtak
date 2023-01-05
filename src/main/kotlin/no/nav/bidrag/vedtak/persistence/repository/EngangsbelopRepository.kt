package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Engangsbelop
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface EngangsbelopRepository : CrudRepository<Engangsbelop, Int?>{

  @Query(
    "select eb from Engangsbelop eb where eb.vedtak.id = :vedtakId"
  )
  fun hentAlleEngangsbelopForVedtak(vedtakId: Int): List<Engangsbelop>
}
