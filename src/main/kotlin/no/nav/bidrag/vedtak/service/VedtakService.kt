package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.dto.VedtakDto
import org.springframework.stereotype.Service

@Service
class VedtakService (val persistenceService: PersistenceService) {

  fun finnVedtak(vedtakId: Int): VedtakDto {
    return persistenceService.henteVedtak(vedtakId)
  }

  fun oprettNyttVedtak(request: NyttVedtakRequest): VedtakDto {
    val vedtakDto = VedtakDto(opprettet_av = request.opprettet_av, enhetsnummer = request.enhetsnummer)
    return persistenceService.lagreVedtak(vedtakDto)
  }
}
