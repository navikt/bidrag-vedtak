package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.periode.OpprettPeriodeRequest
import no.nav.bidrag.vedtak.api.periode.toPeriodeDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PeriodeService(val persistenceService: PersistenceService) {

  fun opprettPeriode(request: OpprettPeriodeRequest) = persistenceService.opprettPeriode(request.toPeriodeDto())

  fun hentPeriode(periodeId: Int) = persistenceService.hentPeriode(periodeId)

  fun hentAllePerioderForStonadsendring(stonadsendringId: Int) = persistenceService.hentAllePerioderForStonadsendring(stonadsendringId)
}
