package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.OppretteNyPeriodeRequest
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

  fun opprettNyPeriode(request: OppretteNyPeriodeRequest): String {
    val periodeDto = PeriodeDto(opprettet_av = request.opprettet_av, enhetsnummer = request.enhetsnummer)
    val opprettetPeriode = periodePersistenceService.lagrePeriode(periodeDto)
    return opprettetPeriode.toString()
  }
}
