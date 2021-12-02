package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Behandlingsreferanse
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface BehandlingsreferanseRepository : CrudRepository<Behandlingsreferanse, Int?>{

  @Query(
    "select be from Behandlingsreferanse be where be.vedtak.vedtakId = :vedtakId")
  fun hentAlleBehandlingsreferanserForVedtak(vedtakId: Int): List<Behandlingsreferanse>
}
