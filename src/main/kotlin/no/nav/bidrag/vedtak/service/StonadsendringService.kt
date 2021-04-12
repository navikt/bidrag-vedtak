package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.AlleStonadsendringerForVedtakResponse
import no.nav.bidrag.vedtak.api.NyStonadsendringRequest
import no.nav.bidrag.vedtak.api.toStonadsendringDto
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StonadsendringService (val persistenceService: PersistenceService) {

  fun opprettNyStonadsendring(request: NyStonadsendringRequest): StonadsendringDto {
    return persistenceService.opprettNyStonadsendring(request.toStonadsendringDto())
  }

  fun finnEnStonadsendring(stonadsendring_id: Int): StonadsendringDto {
    return persistenceService.finnEnStonadsendring(stonadsendring_id)
  }

  fun finnAlleStonadsendringerForVedtak(vedtakId: Int): AlleStonadsendringerForVedtakResponse {
    return AlleStonadsendringerForVedtakResponse(persistenceService.finnAlleStonadsendringerForVedtak(vedtakId))
  }
}
