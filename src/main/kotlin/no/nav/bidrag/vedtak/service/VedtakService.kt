package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.OppretteNyttVedtakRequest
import no.nav.bidrag.vedtak.dto.VedtakDto
import org.springframework.stereotype.Service

@Service
class VedtakService (val persistenceService: PersistenceService) {

  fun finnVedtakDummy(vedtaksnummer: String): String {
    return vedtaksnummer
  }

  fun finnVedtak(vedtakId: Int): VedtakDto {
    return persistenceService.henteVedtak(vedtakId)
  }

  fun nyttVedtakDummy() {}

  fun oprettNyttVedtak(request: OppretteNyttVedtakRequest): String {
    val vedtakDto = VedtakDto(opprettet_av = request.opprettet_av, enhetsnummer = request.enhetsnummer)
    val opprettetVedtak = persistenceService.lagreVedtak(vedtakDto)
    return opprettetVedtak.toString()
  }
}
