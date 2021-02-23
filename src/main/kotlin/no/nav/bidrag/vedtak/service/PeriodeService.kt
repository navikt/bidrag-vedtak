package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.NyPeriodeRequest
import no.nav.bidrag.vedtak.dto.PeriodeDto
import org.springframework.stereotype.Service

@Service
class PeriodeService (val periodePersistenceService: PeriodePersistenceService) {

  fun finnPeriodeDummy(periodeid: String): String {
    return periodeid
  }

  fun finnPeriode(periodeId: Int): PeriodeDto {
    return periodePersistenceService.hentePeriode(periodeId)
  }

  fun nyPeriodeDummy() {}

  fun opprettNyPeriode(request: NyPeriodeRequest): String {
    val periodeDto = PeriodeDto(opprettetAv = request.opprettetAv, enhetsnummer = request.enhetsnummer)
    val opprettetPeriode = periodePersistenceService.lagrePeriode(periodeDto)
    return opprettetPeriode.toString()
  }
}
