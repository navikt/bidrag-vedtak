package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.AlleGrunnlagForPeriodeResponse
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import org.springframework.stereotype.Service

@Service
class PeriodeGrunnlagService (val persistenceService: PersistenceService) {

  fun hentPeriodeGrunnlag(periodeId: Int, grunnlag_id: Int): PeriodeGrunnlagDto {
    return persistenceService.hentPeriodeGrunnlag(periodeId, grunnlag_id)
  }

  fun hentAlleGrunnlagForPeriode(periodeId: Int): AlleGrunnlagForPeriodeResponse {
    return AlleGrunnlagForPeriodeResponse(persistenceService.hentAlleGrunnlagForPeriode(periodeId))
  }
}
