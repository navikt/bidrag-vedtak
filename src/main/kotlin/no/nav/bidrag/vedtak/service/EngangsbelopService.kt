package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.engangsbelop.OpprettEngangsbelopRequest
import no.nav.bidrag.vedtak.api.engangsbelop.toEngangsbelopDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EngangsbelopService(val persistenceService: PersistenceService) {

  fun opprettEngangsbelop(request: OpprettEngangsbelopRequest) = persistenceService.opprettEngangsbelop(request.toEngangsbelopDto())

  fun hentEngangsbelop(engangsbelop_id: Int) = persistenceService.hentEngangsbelop(engangsbelop_id)

  fun hentAlleEngangsbelopForVedtak(vedtakId: Int) = persistenceService.hentAlleEngangsbelopForVedtak(vedtakId)
}