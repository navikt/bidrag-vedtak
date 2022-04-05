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
import no.nav.bidrag.vedtak.bo.toBehandlingsreferanseBo
import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import no.nav.bidrag.vedtak.persistence.entity.toEngangsbelopEntity
import no.nav.bidrag.vedtak.persistence.entity.toGrunnlagEntity
import no.nav.bidrag.vedtak.persistence.entity.toPeriodeEntity
import no.nav.bidrag.vedtak.persistence.entity.toStonadsendringEntity
import no.nav.bidrag.vedtak.persistence.entity.toVedtakEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class VedtakService(val persistenceService: PersistenceService, val hendelserService: HendelserService) {

  // Opprett vedtak (alle tabeller)
  fun opprettVedtak(vedtakRequest: OpprettVedtakRequestDto): Int {

    // Opprett vedtak
    val opprettetVedtak = persistenceService.opprettVedtak(vedtakRequest.toVedtakEntity())

    val grunnlagIdRefMap = mutableMapOf<String, Int>()

    // Grunnlag
    vedtakRequest.grunnlagListe.forEach {
      val opprettetGrunnlagId = opprettGrunnlag(it, opprettetVedtak)
      grunnlagIdRefMap[it.referanse] = opprettetGrunnlagId
    }

    // Stønadsendring
    vedtakRequest.stonadsendringListe?.forEach { opprettStonadsendring(it, opprettetVedtak, grunnlagIdRefMap) }

    // Engangsbelop
    var lopenr: Int = 0
    vedtakRequest.engangsbelopListe?.forEach {
      lopenr ++
      opprettEngangsbelop(it, opprettetVedtak, lopenr, grunnlagIdRefMap) }

    // Behandlingsreferanse
    vedtakRequest.behandlingsreferanseListe?.forEach { opprettBehandlingsreferanse(it, opprettetVedtak.vedtakId) }

    if (vedtakRequest.stonadsendringListe?.isNotEmpty() == true) {
      hendelserService.opprettHendelse(vedtakRequest, opprettetVedtak.vedtakId, opprettetVedtak.opprettetTimestamp)
    }

    return opprettetVedtak.vedtakId
  }

  // Opprett grunnlag
  private fun opprettGrunnlag(grunnlagRequest: OpprettGrunnlagRequestDto, vedtak: Vedtak) =
    persistenceService.opprettGrunnlag(grunnlagRequest.toGrunnlagEntity(vedtak))


  // Opprett stønadsendring
  private fun opprettStonadsendring(stonadsendringRequest: OpprettStonadsendringRequestDto, vedtak: Vedtak, grunnlagIdRefMap: Map<String, Int>) {
    val opprettetStonadsendring = persistenceService.opprettStonadsendring(stonadsendringRequest.toStonadsendringEntity(vedtak))

    // Periode
    stonadsendringRequest.periodeListe.forEach { opprettPeriode(it, opprettetStonadsendring, grunnlagIdRefMap) }
  }

  // Opprett Engangsbelop
  private fun opprettEngangsbelop(engangsbelopRequest: OpprettEngangsbelopRequestDto, vedtak: Vedtak, lopenr: Int, grunnlagIdRefMap: Map<String, Int>) {
    val opprettetEngangsbelopId = persistenceService.opprettEngangsbelop(engangsbelopRequest.toEngangsbelopEntity(vedtak, lopenr))

    // EngangsbelopGrunnlag
    engangsbelopRequest.grunnlagReferanseListe.forEach {
      val grunnlagId = grunnlagIdRefMap.getOrDefault(it, 0)
      if (grunnlagId == 0) {
        val feilmelding = "grunnlagReferanse $it ikke funnet i intern mappingtabell"
        LOGGER.error(feilmelding)
        throw IllegalArgumentException(feilmelding)
      } else {
        val engangsbelopGrunnlagBo = EngangsbelopGrunnlagBo(
          engangsbelopId = opprettetEngangsbelopId,
          grunnlagId = grunnlagId
        )
        persistenceService.opprettEngangsbelopGrunnlag(engangsbelopGrunnlagBo)
      }
    }
  }

  // Opprett periode
  private fun opprettPeriode(periodeRequest: OpprettVedtakPeriodeRequestDto, stonadsendring: Stonadsendring, grunnlagIdRefMap: Map<String, Int>) {
    val opprettetPeriode = persistenceService.opprettPeriode(periodeRequest.toPeriodeEntity(stonadsendring))

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
        persistenceService.opprettPeriodeGrunnlag(opprettetPeriode.periodeId, grunnlagId)
//        persistenceService.opprettPeriodeGrunnlag(periodeGrunnlagBo)
      }
    }
  }

  // Opprett behandlingsreferanse
  private fun opprettBehandlingsreferanse(behandlingsreferanseRequest: OpprettBehandlingsreferanseRequestDto, vedtakId: Int) =
    persistenceService.opprettBehandlingsreferanse(behandlingsreferanseRequest.toBehandlingsreferanseBo(vedtakId)
    )



  // Hent vedtaksdata
  fun hentVedtak(vedtakId: Int): VedtakDto {
    val vedtak = persistenceService.hentVedtak(vedtakId)
    val grunnlagResponseListe = ArrayList<GrunnlagDto>()
    val grunnlagBoListe = persistenceService.hentAlleGrunnlagForVedtak(vedtak.vedtakId)
    grunnlagBoListe.forEach {
      grunnlagResponseListe.add(
        GrunnlagDto(it.grunnlagId, it.referanse, GrunnlagType.valueOf(it.type), it.innhold)
      )
    }
    val stonadsendringBoListe = persistenceService.hentAlleStonadsendringerForVedtak(vedtak.vedtakId)
    val engangsbelopBoListe = persistenceService.hentAlleEngangsbelopForVedtak(vedtak.vedtakId)
    val behandlingsreferanseBoListe = persistenceService.hentAlleBehandlingsreferanserForVedtak(vedtak.vedtakId)
    val behandlingsreferanseResponseListe = ArrayList<BehandlingsreferanseDto>()
    behandlingsreferanseBoListe.forEach {
      behandlingsreferanseResponseListe.add(
        BehandlingsreferanseDto(it.kilde, it.referanse)
      )
    }

    return VedtakDto(
      vedtak.vedtakId,
      VedtakType.valueOf(vedtak.vedtakType),
      vedtak.opprettetAv,
      vedtak.vedtakDato,
      vedtak.enhetId,
      vedtak.opprettetTimestamp,
      grunnlagResponseListe,
      hentStonadsendringerTilVedtak(stonadsendringBoListe),
      hentEngangsbelopTilVedtak(engangsbelopBoListe),
      behandlingsreferanseResponseListe
    )
  }

  private fun hentStonadsendringerTilVedtak(stonadsendringBoListe: List<StonadsendringBo>): List<StonadsendringDto> {
    val stonadsendringDtoListe = ArrayList<StonadsendringDto>()
    stonadsendringBoListe.forEach {
      val periodeDtoListe = persistenceService.hentAllePerioderForStonadsendring(it.stonadsendringId)
      stonadsendringDtoListe.add(
        StonadsendringDto(
          StonadType.valueOf(it.stonadType),
          it.sakId,
          it.behandlingId,
          it.skyldnerId,
          it.kravhaverId,
          it.mottakerId,
          hentPerioderTilVedtak(periodeDtoListe)
        )
      )
    }
    return stonadsendringDtoListe
  }

  private fun hentPerioderTilVedtak(periodeBoListe: List<PeriodeBo>): List<VedtakPeriodeDto> {
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

  private fun hentEngangsbelopTilVedtak(engangsbelopBoListe: List<EngangsbelopBo>): List<EngangsbelopDto> {
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

  companion object {
    private val LOGGER = LoggerFactory.getLogger(VedtakService::class.java)
  }
}
