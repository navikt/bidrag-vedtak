package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.AllePerioderForStonadResponse
import no.nav.bidrag.vedtak.api.NyPeriodeRequest
import no.nav.bidrag.vedtak.api.toPeriodeDto
import no.nav.bidrag.vedtak.dto.PeriodeDto
import org.springframework.stereotype.Service

@Service
class PeriodeService (val persistenceService: PersistenceService) {

  fun opprettNyPeriode(request: NyPeriodeRequest): PeriodeDto {
    return persistenceService.opprettNyPeriode(request.toPeriodeDto())
  }

  fun finnPeriode(periodeId: Int): PeriodeDto {
    return persistenceService.finnPeriode(periodeId)
  }

  fun finnAllePerioderForStonad(stonadIdListe: List<Int>): AllePerioderForStonadResponse {
    return AllePerioderForStonadResponse(persistenceService.finnAllePerioderForStonad(stonadIdListe))
  }

}
