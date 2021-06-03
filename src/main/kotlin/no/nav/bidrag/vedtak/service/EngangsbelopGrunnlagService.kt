package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.engangsbelopgrunnlag.OpprettEngangsbelopGrunnlagRequest
import no.nav.bidrag.vedtak.api.engangsbelopgrunnlag.toEngangsbelopGrunnlagDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EngangsbelopGrunnlagService(val persistenceService: PersistenceService) {

  fun opprettEngangsbelopGrunnlag(request: OpprettEngangsbelopGrunnlagRequest) = persistenceService.opprettEngangsbelopGrunnlag(request.toEngangsbelopGrunnlagDto())

  fun hentEngangsbelopGrunnlag(engangsbelopId: Int, grunnlag_id: Int) = persistenceService.hentEngangsbelopGrunnlag(engangsbelopId, grunnlag_id)

  fun hentAlleGrunnlagForEngangsbelop(engangsbelopId: Int) = persistenceService.hentAlleGrunnlagForEngangsbelop(engangsbelopId)
}
