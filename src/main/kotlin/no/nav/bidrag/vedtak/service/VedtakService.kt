package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.AlleVedtakResponse
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.api.OpprettGrunnlagRequest
import no.nav.bidrag.vedtak.api.OpprettPeriodeRequest
import no.nav.bidrag.vedtak.api.OpprettStonadsendringRequest
import no.nav.bidrag.vedtak.api.OpprettVedtakRequest
import no.nav.bidrag.vedtak.api.OpprettVedtakResponse
import no.nav.bidrag.vedtak.api.toGrunnlagDto
import no.nav.bidrag.vedtak.api.toPeriodeDto
import no.nav.bidrag.vedtak.api.toStonadsendringDto
import no.nav.bidrag.vedtak.controller.PeriodeController
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class VedtakService (val persistenceService: PersistenceService) {

  private val grunnlagIdRefMap = mutableMapOf<String, Int>()
  private val LOGGER = LoggerFactory.getLogger(PeriodeController::class.java)

  fun opprettNyttVedtak(request: NyttVedtakRequest): VedtakDto {
    val vedtakDto = VedtakDto(enhetId = request.enhetId, saksbehandlerId = request.saksbehandlerId)
    return persistenceService.opprettNyttVedtak(vedtakDto)
  }

  fun finnEttVedtak(vedtak_id: Int): VedtakDto {
    return persistenceService.finnEttVedtak(vedtak_id)
  }

  fun finnAlleVedtak(): AlleVedtakResponse {
    return AlleVedtakResponse(persistenceService.finnAlleVedtak())
  }

  // Opprett komplett vedtak (alle tabeller)
  fun opprettKomplettVedtak(vedtakRequest: OpprettVedtakRequest): OpprettVedtakResponse {

    // Opprett vedtak
    val vedtakDto = VedtakDto(enhetId = vedtakRequest.enhetId, saksbehandlerId = vedtakRequest.saksbehandlerId)
    val opprettetVedtak = persistenceService.opprettNyttVedtak(vedtakDto)

    // Grunnlag
    vedtakRequest.grunnlagListe.forEach {
      val opprettetGrunnlag = opprettGrunnlag(it, opprettetVedtak.vedtakId)
      grunnlagIdRefMap[it.grunnlagReferanse] = opprettetGrunnlag.grunnlagId
    }

    // Stønadsendring
    vedtakRequest.stonadsendringListe.forEach { opprettStonadsendring(it, opprettetVedtak.vedtakId) }

    return OpprettVedtakResponse(opprettetVedtak.vedtakId)
  }

  // Opprett grunnlag
  private fun opprettGrunnlag(grunnlagRequest: OpprettGrunnlagRequest, vedtakId: Int): GrunnlagDto {
    return persistenceService.opprettNyttGrunnlag(grunnlagRequest.toGrunnlagDto(vedtakId))
  }

  // Opprett stønadsendring
  private fun opprettStonadsendring(stonadsendringRequest: OpprettStonadsendringRequest, vedtakId: Int) {
    val opprettetStonadsendring = persistenceService.opprettNyStonadsendring(stonadsendringRequest.toStonadsendringDto(vedtakId))

    // Periode
    stonadsendringRequest.periodeListe.forEach { opprettPeriode(it, opprettetStonadsendring.stonadsendringId) }
  }

  // Opprett periode
  private fun opprettPeriode(periodeRequest: OpprettPeriodeRequest, stonadsendringId: Int) {
    val opprettetPeriode = persistenceService.opprettNyPeriode(periodeRequest.toPeriodeDto(stonadsendringId))

    // PeriodeGrunnlag
    periodeRequest.grunnlagReferanseListe.forEach {
      val grunnlagId = grunnlagIdRefMap.getOrDefault(it.grunnlagReferanse, 0)
      if (grunnlagId == 0) {
        val feilmelding = "grunnlagReferanse ${it.grunnlagReferanse} ikke funnet i intern mappingtabell"
        LOGGER.error(feilmelding)
        throw IllegalArgumentException(feilmelding)
      } else {
        val periodeGrunnlagDto = PeriodeGrunnlagDto(periodeId = opprettetPeriode.periodeId, grunnlagId = grunnlagId, grunnlagValgt = it.grunnlagValgt)
        persistenceService.opprettNyttPeriodeGrunnlag(periodeGrunnlagDto)
      }
    }
  }
}
