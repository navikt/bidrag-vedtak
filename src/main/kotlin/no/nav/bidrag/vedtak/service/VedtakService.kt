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
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.vedtak.bo.EngangsbelopGrunnlagBo
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import no.nav.bidrag.vedtak.persistence.entity.Engangsbelop
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import no.nav.bidrag.vedtak.persistence.entity.toBehandlingsreferanseEntity
import no.nav.bidrag.vedtak.persistence.entity.toEngangsbelopEntity
import no.nav.bidrag.vedtak.persistence.entity.toGrunnlagDto
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
      grunnlagIdRefMap[it.referanse] = opprettetGrunnlagId.grunnlagId
    }

    // Stønadsendring
    vedtakRequest.stonadsendringListe?.forEach { opprettStonadsendring(it, opprettetVedtak, grunnlagIdRefMap) }

    // Engangsbelop
    var lopenr: Int = 0
    vedtakRequest.engangsbelopListe?.forEach {
      lopenr ++
      opprettEngangsbelop(it, opprettetVedtak, lopenr, grunnlagIdRefMap) }

    // Behandlingsreferanse
    vedtakRequest.behandlingsreferanseListe?.forEach { opprettBehandlingsreferanse(it, opprettetVedtak) }

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
    val opprettetEngangsbelop = persistenceService.opprettEngangsbelop(engangsbelopRequest.toEngangsbelopEntity(vedtak, lopenr))

    // EngangsbelopGrunnlag
    engangsbelopRequest.grunnlagReferanseListe.forEach {
      val grunnlagId = grunnlagIdRefMap.getOrDefault(it, 0)
      if (grunnlagId == 0) {
        val feilmelding = "grunnlagReferanse $it ikke funnet i intern mappingtabell"
        LOGGER.error(feilmelding)
        throw IllegalArgumentException(feilmelding)
      } else {
        persistenceService.opprettEngangsbelopGrunnlag(EngangsbelopGrunnlagBo(opprettetEngangsbelop.engangsbelopId, grunnlagId))
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
        persistenceService.opprettPeriodeGrunnlag(periodeGrunnlagBo)
      }
    }
  }

  // Opprett behandlingsreferanse
  private fun opprettBehandlingsreferanse(behandlingsreferanseRequest: OpprettBehandlingsreferanseRequestDto, vedtak: Vedtak) =
    persistenceService.opprettBehandlingsreferanse(behandlingsreferanseRequest.toBehandlingsreferanseEntity(vedtak)
    )


  // Hent vedtaksdata
  fun hentVedtak(vedtakId: Int): VedtakDto {
    val vedtak = persistenceService.hentVedtak(vedtakId)
    val grunnlagDtoListe = ArrayList<GrunnlagDto>()
    val grunnlagListe = persistenceService.hentAlleGrunnlagForVedtak(vedtak.vedtakId)
    grunnlagListe.forEach {
      grunnlagDtoListe.add(it.toGrunnlagDto())
    }
    val stonadsendringListe = persistenceService.hentAlleStonadsendringerForVedtak(vedtak.vedtakId)
    val engangsbelopListe = persistenceService.hentAlleEngangsbelopForVedtak(vedtak.vedtakId)
    val behandlingsreferanseListe = persistenceService.hentAlleBehandlingsreferanserForVedtak(vedtak.vedtakId)
    val behandlingsreferanseResponseListe = ArrayList<BehandlingsreferanseDto>()
    behandlingsreferanseListe.forEach {
      behandlingsreferanseResponseListe.add(
        BehandlingsreferanseDto(it.kilde, it.referanse)
      )
    }

    return VedtakDto(
      vedtakId = vedtak.vedtakId,
      vedtakType = VedtakType.valueOf(vedtak.vedtakType),
      opprettetAv = vedtak.opprettetAv,
      vedtakDato = vedtak.vedtakDato,
      enhetId = vedtak.enhetId,
      opprettetTimestamp = vedtak.opprettetTimestamp,
      grunnlagListe = grunnlagDtoListe,
      stonadsendringListe = hentStonadsendringerTilVedtak(stonadsendringListe),
      engangsbelopListe = hentEngangsbelopTilVedtak(engangsbelopListe),
      behandlingsreferanseListe = behandlingsreferanseResponseListe
    )
  }

  private fun hentStonadsendringerTilVedtak(stonadsendringListe: List<Stonadsendring>): List<StonadsendringDto> {
    val stonadsendringDtoListe = ArrayList<StonadsendringDto>()
    stonadsendringListe.forEach {
      val periodeListe = persistenceService.hentAllePerioderForStonadsendring(it.stonadsendringId)
      stonadsendringDtoListe.add(
        StonadsendringDto(
          stonadType = StonadType.valueOf(it.stonadType),
          sakId = it.sakId,
          behandlingId = it.behandlingId,
          skyldnerId = it.skyldnerId,
          kravhaverId = it.kravhaverId,
          mottakerId = it.mottakerId,
          periodeListe = hentPerioderTilVedtak(periodeListe)
        )
      )
    }
    return stonadsendringDtoListe
  }

  private fun hentPerioderTilVedtak(periodeListe: List<Periode>): List<VedtakPeriodeDto> {
    val periodeResponseListe = ArrayList<VedtakPeriodeDto>()
    periodeListe.forEach { dto ->
      val grunnlagReferanseResponseListe = ArrayList<String>()
      val periodeGrunnlagListe = persistenceService.hentAlleGrunnlagForPeriode(dto.periodeId)
      periodeGrunnlagListe.forEach {
        val grunnlag = persistenceService.hentGrunnlag(it.grunnlag.grunnlagId)
        grunnlagReferanseResponseListe.add(grunnlag.referanse)
      }
      periodeResponseListe.add(
        VedtakPeriodeDto(
          periodeFomDato = dto.periodeFomDato,
          periodeTilDato = dto.periodeTilDato,
          belop = dto.belop,
          valutakode = dto.valutakode.trimEnd(),
          resultatkode = dto.resultatkode,
          grunnlagReferanseListe = grunnlagReferanseResponseListe
        )
      )
    }
    return periodeResponseListe
  }

  private fun hentEngangsbelopTilVedtak(engangsbelopListe: List<Engangsbelop>): List<EngangsbelopDto> {
    val engangsbelopResponseListe = ArrayList<EngangsbelopDto>()
    engangsbelopListe.forEach { dto ->
      val grunnlagReferanseResponseListe = ArrayList<String>()
      val engangsbelopGrunnlagListe = persistenceService.hentAlleGrunnlagForEngangsbelop(dto.engangsbelopId)
      engangsbelopGrunnlagListe.forEach {
        val grunnlag = persistenceService.hentGrunnlag(it.grunnlag.grunnlagId)
        grunnlagReferanseResponseListe.add(grunnlag.referanse)
      }
      engangsbelopResponseListe.add(
        EngangsbelopDto(
          engangsbelopId = dto.engangsbelopId,
          lopenr = dto.lopenr,
          endrerEngangsbelopId = dto.endrerEngangsbelopId,
          type = dto.type,
          skyldnerId = dto.skyldnerId,
          kravhaverId = dto.kravhaverId,
          mottakerId = dto.mottakerId,
          belop = dto.belop,
          valutakode = dto.valutakode,
          resultatkode = dto.resultatkode,
          grunnlagReferanseListe = grunnlagReferanseResponseListe
        )
      )
    }
    return engangsbelopResponseListe
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(VedtakService::class.java)
  }
}
