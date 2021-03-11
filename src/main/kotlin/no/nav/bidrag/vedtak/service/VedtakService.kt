package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.AlleVedtakResponse
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.dto.VedtakDto
import org.springframework.stereotype.Service

@Service
class VedtakService (val persistenceService: PersistenceService) {

  fun opprettNyttVedtak(request: NyttVedtakRequest): VedtakDto {
    val vedtakDto = VedtakDto(enhetsnummer = request.enhetsnummer, opprettetAv = request.opprettetAv)
    return persistenceService.opprettNyttVedtak(vedtakDto)
  }

  fun finnEttVedtak(vedtak_id: Int): VedtakDto {
    return persistenceService.finnEttVedtak(vedtak_id)
  }

  fun finnAlleVedtak(): AlleVedtakResponse {
    return AlleVedtakResponse(persistenceService.finnAlleVedtak())
  }
}
