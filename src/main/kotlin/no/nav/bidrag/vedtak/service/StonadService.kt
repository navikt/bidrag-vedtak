package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.AlleStonaderResponse
import no.nav.bidrag.vedtak.api.NyStonadRequest
import no.nav.bidrag.vedtak.api.toStonadDto
import no.nav.bidrag.vedtak.dto.StonadDto
import org.springframework.stereotype.Service

@Service
class StonadService (val persistenceService: PersistenceService) {

  fun opprettNyStonad(request: NyStonadRequest): StonadDto {
    return persistenceService.opprettNyStonad(request.toStonadDto())
  }

  fun finnEnStonad(stonad_id: Int): StonadDto {
    return persistenceService.finnEnStonad(stonad_id)
  }

  fun finnAlleStonader(): AlleStonaderResponse {
    return AlleStonaderResponse(persistenceService.finnAlleStonader())
  }
}
