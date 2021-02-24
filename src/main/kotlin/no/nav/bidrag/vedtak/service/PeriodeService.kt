package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.AllePerioderForStonadResponse
import no.nav.bidrag.vedtak.api.NyPeriodeRequest
import no.nav.bidrag.vedtak.dto.PeriodeDto
import org.springframework.stereotype.Service

@Service
class PeriodeService (val periodePersistenceService: PeriodePersistenceService) {

  fun opprettNyPeriode(request: NyPeriodeRequest): PeriodeDto {
    val periodeDto = PeriodeDto(opprettetAv = request.opprettetAv, enhetsnummer = request.enhetsnummer)
    return periodePersistenceService.opprettNyPeriode(periodeDto)
  }

  fun finnPeriode(periodeId: Int): PeriodeDto {
    return periodePersistenceService.finnPeriode(periodeId)
  }

  fun finnAllePerioderForStonad(stonadId: Int): AllePerioderForStonadResponse {
    return AllePerioderForStonadResponse(periodePersistenceService.finnAllePerioderForStonad(stonadId))
  }

}
