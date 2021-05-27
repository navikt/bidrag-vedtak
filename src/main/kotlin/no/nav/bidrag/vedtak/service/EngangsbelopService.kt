package no.nav.bidrag.vedtak.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EngangsbelopService(val persistenceService: PersistenceService) {

  fun hentEngangsbelop(engangsbelop_id: Int) = persistenceService.hentEngangsbelop(engangsbelop_id)

  fun hentAlleEngangsbelopForVedtak(vedtakId: Int) = persistenceService.hentAlleEngangsbelopForVedtak(vedtakId)
}