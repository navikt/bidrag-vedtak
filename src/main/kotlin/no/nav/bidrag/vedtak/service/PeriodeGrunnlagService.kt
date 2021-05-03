package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.periodegrunnlag.OpprettPeriodeGrunnlagRequest
import no.nav.bidrag.vedtak.api.periodegrunnlag.toPeriodeGrunnlagDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PeriodeGrunnlagService(val persistenceService: PersistenceService) {

  fun opprettPeriodeGrunnlag(request: OpprettPeriodeGrunnlagRequest) = persistenceService.opprettPeriodeGrunnlag(request.toPeriodeGrunnlagDto())

  fun hentPeriodeGrunnlag(periodeId: Int, grunnlag_id: Int) = persistenceService.hentPeriodeGrunnlag(periodeId, grunnlag_id)

  fun hentAllePeriodeGrunnlagForPeriode(periodeId: Int) = persistenceService.hentAllePeriodeGrunnlagForPeriode(periodeId)
}
