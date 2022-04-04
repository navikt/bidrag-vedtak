package no.nav.bidrag.vedtak.service

import no.nav.bidrag.behandling.felles.dto.vedtak.BehandlingsreferanseDto
import no.nav.bidrag.behandling.felles.dto.vedtak.EngangsbelopDto
import no.nav.bidrag.behandling.felles.dto.vedtak.GrunnlagDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettBehandlingsreferanseRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettEngangsbelopRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettGrunnlagRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettStonadsendringRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.StonadsendringDto
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakDto
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakPeriodeDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagType
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.vedtak.bo.EngangsbelopBo
import no.nav.bidrag.vedtak.bo.EngangsbelopGrunnlagBo
import no.nav.bidrag.vedtak.bo.PeriodeBo
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import no.nav.bidrag.vedtak.bo.StonadsendringBo
import no.nav.bidrag.vedtak.bo.VedtakBo
import no.nav.bidrag.vedtak.bo.toBehandlingsreferanseBo
import no.nav.bidrag.vedtak.bo.toEngangsbelopBo
import no.nav.bidrag.vedtak.bo.toGrunnlagBo
import no.nav.bidrag.vedtak.bo.toPeriodeBo
import no.nav.bidrag.vedtak.bo.toStonadsendringBo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class VedtakService(val persistenceService: PersistenceService, val hendelserService: HendelserService) {

  fun hentVedtak(vedtakId: Int): VedtakDto {
    val vedtakDto = persistenceService.hentVedtak(vedtakId)
    val grunnlagResponseListe = ArrayList<GrunnlagDto>()
    val grunnlagDtoListe = persistenceService.hentAlleGrunnlagForVedtak(vedtakDto.vedtakId)
    grunnlagDtoListe.forEach {
      grunnlagResponseListe.add(
        GrunnlagDto(it.grunnlagId, it.referanse, GrunnlagType.valueOf(it.type), it.innhold)
      )
    }
    val stonadsendringDtoListe = persistenceService.hentAlleStonadsendringerForVedtak(vedtakDto.vedtakId)
    val engangsbelopDtoListe = persistenceService.hentAlleEngangsbelopForVedtak(vedtakDto.vedtakId)
    val behandlingsreferanseResponseListe = ArrayList<BehandlingsreferanseDto>()
    val behandlingsreferanseDtoListe = persistenceService.hentAlleBehandlingsreferanserForVedtak(vedtakDto.vedtakId)
    behandlingsreferanseDtoListe.forEach {
      behandlingsreferanseResponseListe.add(
        BehandlingsreferanseDto(it.kilde, it.referanse)
      )
    }

    return VedtakDto(
      vedtakDto.vedtakId,
      VedtakType.valueOf(vedtakDto.vedtakType),
      vedtakDto.opprettetAv,
      vedtakDto.vedtakDato,
      vedtakDto.enhetId,
      vedtakDto.opprettetTimestamp,
      grunnlagResponseListe,
      finnStonadsendringerTilVedtak(stonadsendringDtoListe),
      finnEngangsbelopTilVedtak(engangsbelopDtoListe),
      behandlingsreferanseResponseListe
    )
  }

  private fun finnStonadsendringerTilVedtak(stonadsendringBoListe: List<StonadsendringBo>): List<StonadsendringDto> {
    val stonadsendringResponseListe = ArrayList<StonadsendringDto>()
    stonadsendringBoListe.forEach {
      val periodeDtoListe = persistenceService.hentAllePerioderForStonadsendring(it.stonadsendringId)
      stonadsendringResponseListe.add(
        StonadsendringDto(
          StonadType.valueOf(it.stonadType),
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

  private fun finnPerioderTilVedtak(periodeBoListe: List<PeriodeBo>): List<VedtakPeriodeDto> {
    val periodeResponseListe = ArrayList<VedtakPeriodeDto>()
    periodeBoListe.forEach { dto ->
      val grunnlagReferanseResponseListe = ArrayList<String>()
      val periodeGrunnlagListe = persistenceService.hentAlleGrunnlagForPeriode(dto.periodeId)
      periodeGrunnlagListe.forEach {
        val grunnlag = persistenceService.hentGrunnlag(it.grunnlagId)
        grunnlagReferanseResponseListe.add(grunnlag.referanse)
      }
      periodeResponseListe.add(
        VedtakPeriodeDto(
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

  private fun finnEngangsbelopTilVedtak(engangsbelopBoListe: List<EngangsbelopBo>): List<EngangsbelopDto> {
    val engangsbelopResponseListe = ArrayList<EngangsbelopDto>()
    engangsbelopBoListe.forEach { dto ->
      val grunnlagReferanseResponseListe = ArrayList<String>()
      val engangsbelopGrunnlagListe = persistenceService.hentAlleGrunnlagForEngangsbelop(dto.engangsbelopId)
      engangsbelopGrunnlagListe.forEach {
        val grunnlag = persistenceService.hentGrunnlag(it.grunnlagId)
        grunnlagReferanseResponseListe.add(grunnlag.referanse)
      }
      engangsbelopResponseListe.add(
        EngangsbelopDto(
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
  fun opprettVedtak(vedtakRequest: OpprettVedtakRequestDto): Int {

    val grunnlagIdRefMap = mutableMapOf<String, Int>()

    // Opprett vedtak
    val vedtakBo = VedtakBo(
      vedtakType = vedtakRequest.vedtakType.toString(),
      opprettetAv = vedtakRequest.opprettetAv,
      vedtakDato = vedtakRequest.vedtakDato,
      enhetId = vedtakRequest.enhetId)
    val opprettetVedtak = persistenceService.opprettVedtak(vedtakBo)
    var lopenr: Int = 0

    // Grunnlag
    vedtakRequest.grunnlagListe.forEach {
      val opprettetGrunnlag = opprettGrunnlag(it, opprettetVedtak.vedtakId)
      grunnlagIdRefMap[it.referanse] = opprettetGrunnlag.grunnlagId
    }

    // Stønadsendring
    vedtakRequest.stonadsendringListe?.forEach { opprettStonadsendring(it, opprettetVedtak.vedtakId, grunnlagIdRefMap) }

    // Engangsbelop
    vedtakRequest.engangsbelopListe?.forEach {
      lopenr ++
      opprettEngangsbelop(it, opprettetVedtak.vedtakId, lopenr, grunnlagIdRefMap) }

    // Behandlingsreferanse
    vedtakRequest.behandlingsreferanseListe?.forEach { opprettBehandlingsreferanse(it, opprettetVedtak.vedtakId) }

    if (vedtakRequest.stonadsendringListe?.isNotEmpty() == true) {
      hendelserService.opprettHendelse(vedtakRequest, opprettetVedtak.vedtakId, opprettetVedtak.opprettetTimestamp)
    }

    return opprettetVedtak.vedtakId
  }

  // Opprett grunnlag
  private fun opprettGrunnlag(grunnlagRequest: OpprettGrunnlagRequestDto, vedtakId: Int) =
    persistenceService.opprettGrunnlag(grunnlagRequest.toGrunnlagBo(vedtakId))

  // Opprett stønadsendring
  private fun opprettStonadsendring(stonadsendringRequest: OpprettStonadsendringRequestDto, vedtakId: Int, grunnlagIdRefMap: Map<String, Int>) {
    val opprettetStonadsendring = persistenceService.opprettStonadsendring(stonadsendringRequest.toStonadsendringBo(vedtakId))

    // Periode
    stonadsendringRequest.periodeListe.forEach { opprettPeriode(it, opprettetStonadsendring.stonadsendringId, grunnlagIdRefMap) }
  }

  // Opprett Engangsbelop
  private fun opprettEngangsbelop(engangsbelopRequest: OpprettEngangsbelopRequestDto, vedtakId: Int, lopenr: Int, grunnlagIdRefMap: Map<String, Int>) {
    val opprettetEngangsbelop = persistenceService.opprettEngangsbelop(engangsbelopRequest.toEngangsbelopBo(vedtakId, lopenr))

    // EngangsbelopGrunnlag
    engangsbelopRequest.grunnlagReferanseListe.forEach {
      val grunnlagId = grunnlagIdRefMap.getOrDefault(it, 0)
      if (grunnlagId == 0) {
        val feilmelding = "grunnlagReferanse $it ikke funnet i intern mappingtabell"
        LOGGER.error(feilmelding)
        throw IllegalArgumentException(feilmelding)
      } else {
        val engangsbelopGrunnlagBo = EngangsbelopGrunnlagBo(
          engangsbelopId = opprettetEngangsbelop.engangsbelopId,
          grunnlagId = grunnlagId
        )
        persistenceService.opprettEngangsbelopGrunnlag(engangsbelopGrunnlagBo)
      }
    }
  }

  // Opprett periode
  private fun opprettPeriode(periodeRequest: OpprettVedtakPeriodeRequestDto, stonadsendringId: Int, grunnlagIdRefMap: Map<String, Int>) {
    val opprettetPeriode = persistenceService.opprettPeriode(periodeRequest.toPeriodeBo(stonadsendringId))

    // PeriodeGrunnlag
    periodeRequest.grunnlagReferanseListe.forEach {
      val grunnlagId = grunnlagIdRefMap.getOrDefault(it, 0)
      if (grunnlagId == 0) {
        val feilmelding = "grunnlagReferanse $it ikke funnet i intern mappingtabell"
        LOGGER.error(feilmelding)
        throw IllegalArgumentException(feilmelding)
      } else {
        val periodeGrunnlagBo = PeriodeGrunnlagBo(
          periodeId = opprettetPeriode.periodeId,
          grunnlagId = grunnlagId
        )
        persistenceService.opprettPeriodeGrunnlag(periodeGrunnlagBo)
      }
    }
  }

  // Opprett behandlingsreferanse
  private fun opprettBehandlingsreferanse(behandlingsreferanseRequest: OpprettBehandlingsreferanseRequestDto, vedtakId: Int) =
    persistenceService.opprettBehandlingsreferanse(behandlingsreferanseRequest.toBehandlingsreferanseBo(vedtakId)
    )

  companion object {
    private val LOGGER = LoggerFactory.getLogger(VedtakService::class.java)
  }
}