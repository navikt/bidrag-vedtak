package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.behandlingsreferanse.HentBehandlingsreferanseResponse
import no.nav.bidrag.vedtak.api.behandlingsreferanse.OpprettBehandlingsreferanseRequest
import no.nav.bidrag.vedtak.api.behandlingsreferanse.toBehandlingsreferanseDto
import no.nav.bidrag.vedtak.api.engangsbelop.HentEngangsbelopResponse
import no.nav.bidrag.vedtak.api.engangsbelop.OpprettEngangsbelopRequest
import no.nav.bidrag.vedtak.api.engangsbelop.toEngangsbelopDto
import no.nav.bidrag.vedtak.api.grunnlag.HentGrunnlagReferanseResponse
import no.nav.bidrag.vedtak.api.grunnlag.HentGrunnlagResponse
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagRequest
import no.nav.bidrag.vedtak.api.grunnlag.toGrunnlagDto
import no.nav.bidrag.vedtak.api.periode.HentPeriodeResponse
import no.nav.bidrag.vedtak.api.periode.OpprettPeriodeRequest
import no.nav.bidrag.vedtak.api.periode.toPeriodeDto
import no.nav.bidrag.vedtak.api.stonadsendring.HentStonadsendringResponse
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettStonadsendringRequest
import no.nav.bidrag.vedtak.api.stonadsendring.toStonadsendringDto
import no.nav.bidrag.vedtak.api.vedtak.HentVedtakResponse
import no.nav.bidrag.vedtak.api.vedtak.OpprettVedtakRequest
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

  fun hentVedtak(vedtakId: Int): HentVedtakResponse {
    val vedtakDto = persistenceService.hentVedtak(vedtakId)
    LOGGER.info("Følgende vedtakDto ble funnet: $vedtakDto")
    val grunnlagResponseListe = ArrayList<HentGrunnlagResponse>()
    val grunnlagDtoListe = persistenceService.hentAlleGrunnlagForVedtak(vedtakDto.vedtakId)
    grunnlagDtoListe.forEach {
      grunnlagResponseListe.add(
        HentGrunnlagResponse(it.grunnlagId, it.grunnlagReferanse, it.grunnlagType, it.grunnlagInnhold)
      )
    }
    LOGGER.info("Følgende grunnlagDtoListe ble funnet: $grunnlagDtoListe")

    val stonadsendringDtoListe = persistenceService.hentAlleStonadsendringerForVedtak(vedtakDto.vedtakId)

    LOGGER.info("Følgende stonadsendringDtoListe ble funnet: $stonadsendringDtoListe")

    val engangsbelopDtoListe = persistenceService.hentAlleEngangsbelopForVedtak(vedtakDto.vedtakId)

    LOGGER.info("Følgende engangsbelopDtoListe ble funnet: $engangsbelopDtoListe")

    val behandlingsreferanseResponseListe = ArrayList<HentBehandlingsreferanseResponse>()
    val behandlingsreferanseDtoListe = persistenceService.hentAlleBehandlingsreferanserForVedtak(vedtakDto.vedtakId)
    behandlingsreferanseDtoListe.forEach {
      behandlingsreferanseResponseListe.add(
        HentBehandlingsreferanseResponse(it.kilde, it.referanse)
      )
    }

    LOGGER.info("Følgende behandlingsreferanseResponseListe ble funnet: $behandlingsreferanseResponseListe")

    return HentVedtakResponse(
      vedtakDto.vedtakId,
      vedtakDto.saksbehandlerId,
      vedtakDto.vedtakDato,
      vedtakDto.enhetId,
      vedtakDto.opprettetTimestamp,
      grunnlagResponseListe,
      finnStonadsendringerTilVedtak(stonadsendringDtoListe),
      finnEngangsbelopTilVedtak(engangsbelopDtoListe),
      behandlingsreferanseResponseListe
    )
  }

  private fun finnStonadsendringerTilVedtak(stonadsendringDtoListe: List<StonadsendringDto>): List<HentStonadsendringResponse> {
    val stonadsendringResponseListe = ArrayList<HentStonadsendringResponse>()
    stonadsendringDtoListe.forEach {
      val periodeDtoListe = persistenceService.hentAllePerioderForStonadsendring(it.stonadsendringId)
      stonadsendringResponseListe.add(
        HentStonadsendringResponse(
          it.stonadType,
          it.sakId,
          it.behandlingId,
          it.skyldnerId,
          it.kravhaverId,
          it.mottakerId,
          finnPerioderTilVedtak(periodeDtoListe)
        )
      )
    }
    return stonadsendringResponseListe
  }

  private fun finnPerioderTilVedtak(periodeDtoListe: List<PeriodeDto>): List<HentPeriodeResponse> {
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

  private fun finnEngangsbelopTilVedtak(engangsbelopDtoListe: List<EngangsbelopDto>): List<HentEngangsbelopResponse> {
    val engangsbelopResponseListe = ArrayList<HentEngangsbelopResponse>()
    engangsbelopDtoListe.forEach { dto ->
      val grunnlagReferanseResponseListe = ArrayList<HentGrunnlagReferanseResponse>()
      val engangsbelopGrunnlagListe = persistenceService.hentAlleGrunnlagForEngangsbelop(dto.engangsbelopId)
      engangsbelopGrunnlagListe.forEach {
        val grunnlag = persistenceService.hentGrunnlag(it.grunnlagId)
        grunnlagReferanseResponseListe.add(HentGrunnlagReferanseResponse(grunnlag.grunnlagReferanse))
      }
      engangsbelopResponseListe.add(
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
    return engangsbelopResponseListe
  }

  // Opprett vedtak (alle tabeller)
  fun opprettVedtak(vedtakRequest: OpprettVedtakRequest): Int {

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
    vedtakRequest.stonadsendringListe?.forEach { opprettStonadsendring(it, opprettetVedtak.vedtakId) }

    // Engangsbelop
    vedtakRequest.engangsbelopListe?.forEach {
      lopenr ++
      opprettEngangsbelop(it, opprettetVedtak.vedtakId, lopenr) }

    // Behandlingsreferanse
    vedtakRequest.behandlingsreferanseListe?.forEach { opprettBehandlingsreferanse(it, opprettetVedtak.vedtakId) }

    if (vedtakRequest.stonadsendringListe?.isNotEmpty() == true) {
      hendelserService.opprettHendelse(vedtakRequest, opprettetVedtak.vedtakId, opprettetVedtak.opprettetTimestamp)
    }

    return opprettetVedtak.vedtakId
  }

  // Opprett grunnlag
  private fun opprettGrunnlag(grunnlagRequest: OpprettGrunnlagRequest, vedtakId: Int) =
    persistenceService.opprettGrunnlag(grunnlagRequest.toGrunnlagDto(vedtakId))

  // Opprett stønadsendring
  private fun opprettStonadsendring(stonadsendringRequest: OpprettStonadsendringRequest, vedtakId: Int) {
    val opprettetStonadsendring = persistenceService.opprettStonadsendring(stonadsendringRequest.toStonadsendringDto(vedtakId))

    // Periode
    stonadsendringRequest.periodeListe.forEach { opprettPeriode(it, opprettetStonadsendring.stonadsendringId) }
  }

  // Opprett Engangsbelop
  private fun opprettEngangsbelop(engangsbelopRequest: OpprettEngangsbelopRequest, vedtakId: Int, lopenr: Int) {
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
  private fun opprettPeriode(periodeRequest: OpprettPeriodeRequest, stonadsendringId: Int) {
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