package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.AlleGrunnlagForPeriodeResponse
import no.nav.bidrag.vedtak.api.NyttPeriodeGrunnlagRequest
import no.nav.bidrag.vedtak.api.toPeriodeGrunnlagDto
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PeriodeGrunnlagService (val persistenceService: PersistenceService) {

  fun opprettNyttPeriodeGrunnlag(request: NyttPeriodeGrunnlagRequest): PeriodeGrunnlagDto {
    return persistenceService.opprettNyttPeriodeGrunnlag(request.toPeriodeGrunnlagDto())
  }

  fun hentPeriodeGrunnlag(periodeId: Int, grunnlag_id: Int): PeriodeGrunnlagDto {
    return persistenceService.finnPeriodeGrunnlag(periodeId, grunnlag_id)
  }

  fun hentAlleGrunnlagForPeriode(periodeId: Int): AlleGrunnlagForPeriodeResponse {
    return AlleGrunnlagForPeriodeResponse(persistenceService.finnAlleGrunnlagForPeriode(periodeId))
  }
}
