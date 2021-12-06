package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.behandlingsreferanse.HentBehandlingsreferanseResponse
import no.nav.bidrag.vedtak.api.behandlingsreferanse.OpprettBehandlingsreferanseRequest
import no.nav.bidrag.vedtak.api.behandlingsreferanse.toBehandlingsreferanseDto
import no.nav.bidrag.vedtak.api.engangsbelop.HentEngangsbelopResponse
import no.nav.bidrag.vedtak.api.engangsbelop.OpprettKomplettEngangsbelopRequest
import no.nav.bidrag.vedtak.api.engangsbelop.toEngangsbelopDto
import no.nav.bidrag.vedtak.api.grunnlag.HentGrunnlagReferanseResponse
import no.nav.bidrag.vedtak.api.grunnlag.HentGrunnlagResponse
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagRequest
import no.nav.bidrag.vedtak.api.grunnlag.toGrunnlagDto
import no.nav.bidrag.vedtak.api.periode.HentPeriodeResponse
import no.nav.bidrag.vedtak.api.periode.OpprettKomplettPeriodeRequest
import no.nav.bidrag.vedtak.api.periode.toPeriodeDto
import no.nav.bidrag.vedtak.api.stonadsendring.HentStonadsendringResponse
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettKomplettStonadsendringRequest
import no.nav.bidrag.vedtak.api.stonadsendring.toStonadsendringDto
import no.nav.bidrag.vedtak.api.vedtak.HentKomplettVedtakResponse
import no.nav.bidrag.vedtak.api.vedtak.OpprettKomplettVedtakRequest
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import no.nav.bidrag.vedtak.dto.EngangsbelopGrunnlagDto
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class VedtakService(val persistenceService: PersistenceService, val hendelserService: HendelserService) {

  private val grunnlagIdRefMap = mutableMapOf<String, Int>()

/*  fun opprettVedtak(request: OpprettVedtakRequest): VedtakDto {
    val vedtakDto = VedtakDto(enhetId = request.enhetId, saksbehandlerId = request.saksbehandlerId)
    return persistenceService.opprettVedtak(vedtakDto)
  }

  fun hentVedtak(vedtakId: Int) = persistenceService.hentVedtak(vedtakId)

  fun hentAlleVedtak() = persistenceService.hentAlleVedtak()*/

  fun hentKomplettVedtak(vedtakId: Int): HentKomplettVedtakResponse {
    val vedtakDto = persistenceService.hentVedtak(vedtakId)
    val grunnlagResponseListe = ArrayList<HentGrunnlagResponse>()
    val grunnlagDtoListe = persistenceService.hentAlleGrunnlagForVedtak(vedtakDto.vedtakId)
    grunnlagDtoListe.forEach {
      grunnlagResponseListe.add(
        HentGrunnlagResponse(it.grunnlagId, it.grunnlagReferanse, it.grunnlagType, it.grunnlagInnhold)
      )
    }
    val stonadsendringDtoListe = persistenceService.hentAlleStonadsendringerForVedtak(vedtakDto.vedtakId)
    val engangsbelopDtoListe = persistenceService.hentAlleEngangsbelopForVedtak(vedtakDto.vedtakId)
    val behandlingsreferanseResponseListe = ArrayList<HentBehandlingsreferanseResponse>()
    val behandlingsreferanseDtoListe = persistenceService.hentAlleBehandlingsreferanserForVedtak(vedtakDto.vedtakId)
    behandlingsreferanseDtoListe.forEach {
      behandlingsreferanseResponseListe.add(
        HentBehandlingsreferanseResponse(it.kilde, it.referanse)
      )
    }
    return HentKomplettVedtakResponse(
      vedtakDto.vedtakId,
      vedtakDto.saksbehandlerId,
      vedtakDto.vedtakDato,
      vedtakDto.enhetId,
      vedtakDto.opprettetTimestamp,
      grunnlagResponseListe,
      finnStonadsendringerTilKomplettVedtak(stonadsendringDtoListe),
      finnEngangsbelopTilKomplettVedtak(engangsbelopDtoListe),
      behandlingsreferanseResponseListe
    )
  }

  private fun finnStonadsendringerTilKomplettVedtak(stonadsendringDtoListe: List<StonadsendringDto>): List<HentStonadsendringResponse> {
    val stonadsendringKomplettResponseListe = ArrayList<HentStonadsendringResponse>()
    stonadsendringDtoListe.forEach {
      val periodeDtoListe = persistenceService.hentAllePerioderForStonadsendring(it.stonadsendringId)
      stonadsendringKomplettResponseListe.add(
        HentStonadsendringResponse(
          it.stonadType,
          it.sakId,
          it.behandlingId,
          it.skyldnerId,
          it.kravhaverId,
          it.mottakerId,
          finnPerioderTilKomplettVedtak(periodeDtoListe)
        )
      )
    }
    return stonadsendringKomplettResponseListe
  }

  private fun finnPerioderTilKomplettVedtak(periodeDtoListe: List<PeriodeDto>): List<HentPeriodeResponse> {
    val periodeResponseListe = ArrayList<HentPeriodeResponse>()
    periodeDtoListe.forEach { dto ->
      val grunnlagReferanseResponseListe = ArrayList<HentGrunnlagReferanseResponse>()
      val periodeGrunnlagListe = persistenceService.hentAlleGrunnlagForPeriode(dto.periodeId)
      periodeGrunnlagListe.forEach {
        val grunnlag = persistenceService.hentGrunnlag(it.grunnlagId)
        grunnlagReferanseResponseListe.add(HentGrunnlagReferanseResponse(grunnlag.grunnlagReferanse))
      }
      periodeResponseListe.add(
        HentPeriodeResponse(
          dto.periodeFomDato,
          dto.periodeTilDato,
          dto.belop,
          dto.valutakode.trimEnd(),
          dto.resultatkode,
          grunnlagReferanseResponseListe
        )
      )
    }
    return periodeResponseListe
  }

  private fun finnEngangsbelopTilKomplettVedtak(engangsbelopDtoListe: List<EngangsbelopDto>): List<HentEngangsbelopResponse> {
    val engangsbelopKomplettResponseListe = ArrayList<HentEngangsbelopResponse>()
    engangsbelopDtoListe.forEach { dto ->
      val grunnlagReferanseResponseListe = ArrayList<HentGrunnlagReferanseResponse>()
      val engangsbelopGrunnlagListe = persistenceService.hentAlleGrunnlagForEngangsbelop(dto.engangsbelopId)
      engangsbelopGrunnlagListe.forEach {
        val grunnlag = persistenceService.hentGrunnlag(it.grunnlagId)
        grunnlagReferanseResponseListe.add(HentGrunnlagReferanseResponse(grunnlag.grunnlagReferanse))
      }
      engangsbelopKomplettResponseListe.add(
        HentEngangsbelopResponse(
          dto.engangsbelopId,
          dto.lopenr,
          dto.endrerEngangsbelopId,
          dto.type,
          dto.skyldnerId,
          dto.kravhaverId,
          dto.mottakerId,
          dto.belop,
          dto.valutakode,
          dto.resultatkode,
          grunnlagReferanseResponseListe
        )
      )
    }
    return engangsbelopKomplettResponseListe
  }

  // Opprett komplett vedtak (alle tabeller)
  fun opprettKomplettVedtak(vedtakRequest: OpprettKomplettVedtakRequest): Int {

    // Opprett vedtak
    val vedtakDto = VedtakDto(
      saksbehandlerId = vedtakRequest.saksbehandlerId, vedtakDato = vedtakRequest.vedtakDato, enhetId = vedtakRequest.enhetId)
    val opprettetVedtak = persistenceService.opprettVedtak(vedtakDto)
    var lopenr: Int = 0

    // Grunnlag
    vedtakRequest.grunnlagListe.forEach {
      val opprettetGrunnlag = opprettGrunnlag(it, opprettetVedtak.vedtakId)
      grunnlagIdRefMap[it.grunnlagReferanse] = opprettetGrunnlag.grunnlagId
    }

    // Stønadsendring
    vedtakRequest.stonadsendringListe.forEach { opprettStonadsendring(it, opprettetVedtak.vedtakId) }

    // Engangsbelop
    vedtakRequest.engangsbelopListe.forEach {
      lopenr ++
      opprettEngangsbelop(it, opprettetVedtak.vedtakId, lopenr) }

    // Behandlingsreferanse
    vedtakRequest.behandlingsreferanseListe.forEach { opprettBehandlingsreferanse(it, opprettetVedtak.vedtakId) }

    hendelserService.opprettHendelse(vedtakRequest, opprettetVedtak.vedtakId, opprettetVedtak.opprettetTimestamp)

    return opprettetVedtak.vedtakId
  }

  // Opprett grunnlag
  private fun opprettGrunnlag(grunnlagRequest: OpprettGrunnlagRequest, vedtakId: Int) =
    persistenceService.opprettGrunnlag(grunnlagRequest.toGrunnlagDto(vedtakId))

  // Opprett stønadsendring
  private fun opprettStonadsendring(stonadsendringRequest: OpprettKomplettStonadsendringRequest, vedtakId: Int) {
    val opprettetStonadsendring = persistenceService.opprettStonadsendring(stonadsendringRequest.toStonadsendringDto(vedtakId))

    // Periode
    stonadsendringRequest.periodeListe.forEach { opprettPeriode(it, opprettetStonadsendring.stonadsendringId) }
  }

  // Opprett Engangsbelop
  private fun opprettEngangsbelop(engangsbelopRequest: OpprettKomplettEngangsbelopRequest, vedtakId: Int, lopenr: Int) {
    val opprettetEngangsbelop = persistenceService.opprettEngangsbelop(engangsbelopRequest.toEngangsbelopDto(vedtakId, lopenr))

    // EngangsbelopGrunnlag
    engangsbelopRequest.grunnlagReferanseListe.forEach {
      val grunnlagId = grunnlagIdRefMap.getOrDefault(it.grunnlagReferanse, 0)
      if (grunnlagId == 0) {
        val feilmelding = "grunnlagReferanse ${it.grunnlagReferanse} ikke funnet i intern mappingtabell"
        LOGGER.error(feilmelding)
        throw IllegalArgumentException(feilmelding)
      } else {
        val engangsbelopGrunnlagDto = EngangsbelopGrunnlagDto(
          engangsbelopId = opprettetEngangsbelop.engangsbelopId,
          grunnlagId = grunnlagId
        )
        persistenceService.opprettEngangsbelopGrunnlag(engangsbelopGrunnlagDto)
      }
    }
  }

  // Opprett periode
  private fun opprettPeriode(periodeRequest: OpprettKomplettPeriodeRequest, stonadsendringId: Int) {
    val opprettetPeriode = persistenceService.opprettPeriode(periodeRequest.toPeriodeDto(stonadsendringId))

    // PeriodeGrunnlag
    periodeRequest.grunnlagReferanseListe.forEach {
      val grunnlagId = grunnlagIdRefMap.getOrDefault(it.grunnlagReferanse, 0)
      if (grunnlagId == 0) {
        val feilmelding = "grunnlagReferanse ${it.grunnlagReferanse} ikke funnet i intern mappingtabell"
        LOGGER.error(feilmelding)
        throw IllegalArgumentException(feilmelding)
      } else {
        val periodeGrunnlagDto = PeriodeGrunnlagDto(
          periodeId = opprettetPeriode.periodeId,
          grunnlagId = grunnlagId
        )
        persistenceService.opprettPeriodeGrunnlag(periodeGrunnlagDto)
      }
    }
  }

  // Opprett behandlingsreferanse
  private fun opprettBehandlingsreferanse(behandlingsreferanseRequest: OpprettBehandlingsreferanseRequest, vedtakId: Int) =
    persistenceService.opprettBehandlingsreferanse(behandlingsreferanseRequest.toBehandlingsreferanseDto(vedtakId)
    )

  companion object {
    private val LOGGER = LoggerFactory.getLogger(VedtakService::class.java)
  }
}