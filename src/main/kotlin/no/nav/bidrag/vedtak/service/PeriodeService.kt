package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.AllePerioderForStonadsendringResponse
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

  fun finnAllePerioderForStonadsendring(stonadsendringId: Int): AllePerioderForStonadsendringResponse {
    return AllePerioderForStonadsendringResponse(persistenceService.finnAllePerioderForStonadsendring(stonadsendringId))
  }

}
