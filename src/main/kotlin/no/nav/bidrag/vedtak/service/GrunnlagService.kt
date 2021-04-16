package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.AlleGrunnlagForVedtakResponse
import no.nav.bidrag.vedtak.api.NyttGrunnlagRequest
import no.nav.bidrag.vedtak.api.toGrunnlagDto
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GrunnlagService (val persistenceService: PersistenceService) {

  fun opprettNyttGrunnlag(request: NyttGrunnlagRequest): GrunnlagDto {
    return persistenceService.opprettNyttGrunnlag(request.toGrunnlagDto())
  }

  fun finnGrunnlag(grunnlag_id: Int): GrunnlagDto {
    return persistenceService.finnGrunnlag(grunnlag_id)
  }

  fun finnAlleGrunnlagForVedtak(vedtakId: Int): AlleGrunnlagForVedtakResponse {
    return AlleGrunnlagForVedtakResponse(persistenceService.finnAlleGrunnlagForVedtak(vedtakId))
  }
}
