package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagRequest
import no.nav.bidrag.vedtak.api.grunnlag.toGrunnlagDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GrunnlagService(val persistenceService: PersistenceService) {

  fun opprettGrunnlag(request: OpprettGrunnlagRequest) = persistenceService.opprettGrunnlag(request.toGrunnlagDto())

  fun hentGrunnlag(grunnlag_id: Int) = persistenceService.hentGrunnlag(grunnlag_id)

  fun hentAlleGrunnlagForVedtak(vedtakId: Int) = persistenceService.hentAlleGrunnlagForVedtak(vedtakId)
}
