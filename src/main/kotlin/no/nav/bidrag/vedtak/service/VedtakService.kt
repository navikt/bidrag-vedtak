package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.AlleVedtakResponse
import no.nav.bidrag.vedtak.api.KomplettVedtakResponse
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.api.NyttGrunnlagRequest
import no.nav.bidrag.vedtak.api.NyPeriodeRequest
import no.nav.bidrag.vedtak.api.NyStonadsendringRequest
import no.nav.bidrag.vedtak.api.NyttKomplettVedtakRequest
import no.nav.bidrag.vedtak.api.NyttVedtakResponse
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

  fun finnKomplettVedtak(vedtak_id: Int): KomplettVedtakResponse {
    return persistenceService.finnKomplettVedtak(vedtak_id)
  }

  // Opprett komplett vedtak (alle tabeller)
  fun opprettKomplettVedtak(vedtakRequest: NyttKomplettVedtakRequest): NyttVedtakResponse {

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

    return NyttVedtakResponse(opprettetVedtak.vedtakId)
  }

  // Opprett grunnlag
  private fun opprettGrunnlag(grunnlagRequest: NyttGrunnlagRequest, vedtakId: Int): GrunnlagDto {
    return persistenceService.opprettNyttGrunnlag(grunnlagRequest.toGrunnlagDto(vedtakId))
  }

  // Opprett stønadsendring
  private fun opprettStonadsendring(stonadsendringRequest: NyStonadsendringRequest, vedtakId: Int) {
    val opprettetStonadsendring = persistenceService.opprettNyStonadsendring(stonadsendringRequest.toStonadsendringDto(vedtakId))

    // Periode
    stonadsendringRequest.periodeListe.forEach { opprettPeriode(it, opprettetStonadsendring.stonadsendringId) }
  }

  // Opprett periode
  private fun opprettPeriode(periodeRequest: NyPeriodeRequest, stonadsendringId: Int) {
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