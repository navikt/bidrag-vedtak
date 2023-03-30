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
import no.nav.bidrag.behandling.felles.enums.BehandlingsrefKilde
import no.nav.bidrag.behandling.felles.enums.EngangsbelopType
import no.nav.bidrag.behandling.felles.enums.Innkreving
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakKilde
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
            grunnlagIdRefMap[it.referanse] = opprettetGrunnlagId.id
        }

        // Stønadsendring
        vedtakRequest.stonadsendringListe?.forEach { opprettStonadsendring(it, opprettetVedtak, grunnlagIdRefMap) }

        // Engangsbelop

        vedtakRequest.engangsbelopListe?.forEach { opprettEngangsbelop(it, opprettetVedtak, grunnlagIdRefMap) }

        // Behandlingsreferanse
        vedtakRequest.behandlingsreferanseListe?.forEach { opprettBehandlingsreferanse(it, opprettetVedtak) }

        if (vedtakRequest.stonadsendringListe?.isNotEmpty() == true || vedtakRequest.engangsbelopListe?.isNotEmpty() == true) {
            hendelserService.opprettHendelse(vedtakRequest, opprettetVedtak.id, opprettetVedtak.opprettetTimestamp)
        }

        return opprettetVedtak.id
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
    private fun opprettEngangsbelop(engangsbelopRequest: OpprettEngangsbelopRequestDto, vedtak: Vedtak, grunnlagIdRefMap: Map<String, Int>): Engangsbelop {
        val opprettetEngangsbelop = persistenceService.opprettEngangsbelop(engangsbelopRequest.toEngangsbelopEntity(vedtak))

        // EngangsbelopGrunnlag
        engangsbelopRequest.grunnlagReferanseListe.forEach {
            val grunnlagId = grunnlagIdRefMap.getOrDefault(it, 0)
            if (grunnlagId == 0) {
                val feilmelding = "grunnlagReferanse $it ikke funnet i intern mappingtabell"
                LOGGER.error(feilmelding)
                throw IllegalArgumentException(feilmelding)
            } else {
                persistenceService.opprettEngangsbelopGrunnlag(EngangsbelopGrunnlagBo(opprettetEngangsbelop.id, grunnlagId))
            }
        }
        return opprettetEngangsbelop
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
                    periodeId = opprettetPeriode.id,
                    grunnlagId = grunnlagId
                )
                persistenceService.opprettPeriodeGrunnlag(periodeGrunnlagBo)
            }
        }
    }

    // Opprett behandlingsreferanse
    private fun opprettBehandlingsreferanse(behandlingsreferanseRequest: OpprettBehandlingsreferanseRequestDto, vedtak: Vedtak) =
        persistenceService.opprettBehandlingsreferanse(
            behandlingsreferanseRequest.toBehandlingsreferanseEntity(vedtak)
        )

    // Hent vedtaksdata
    fun hentVedtak(vedtakId: Int): VedtakDto {
        val vedtak = persistenceService.hentVedtak(vedtakId)
        val grunnlagDtoListe = ArrayList<GrunnlagDto>()
        val grunnlagListe = persistenceService.hentAlleGrunnlagForVedtak(vedtak.id)
        grunnlagListe.forEach {
            grunnlagDtoListe.add(it.toGrunnlagDto())
        }
        val stonadsendringListe = persistenceService.hentAlleStonadsendringerForVedtak(vedtak.id)
        val engangsbelopListe = persistenceService.hentAlleEngangsbelopForVedtak(vedtak.id)
        val behandlingsreferanseListe = persistenceService.hentAlleBehandlingsreferanserForVedtak(vedtak.id)
        val behandlingsreferanseResponseListe = ArrayList<BehandlingsreferanseDto>()
        behandlingsreferanseListe.forEach {
            behandlingsreferanseResponseListe.add(
                BehandlingsreferanseDto(BehandlingsrefKilde.valueOf(it.kilde), it.referanse)
            )
        }

        return VedtakDto(
            kilde = VedtakKilde.valueOf(vedtak.kilde),
            type = VedtakType.valueOf(vedtak.type),
            opprettetAv = vedtak.opprettetAv,
            vedtakTidspunkt = vedtak.vedtakTidspunkt,
            enhetId = vedtak.enhetId,
            opprettetTidspunkt = vedtak.opprettetTimestamp,
            utsattTilDato = vedtak.utsattTilDato,
            grunnlagListe = grunnlagDtoListe,
            stonadsendringListe = hentStonadsendringerTilVedtak(stonadsendringListe),
            engangsbelopListe = hentEngangsbelopTilVedtak(engangsbelopListe),
            behandlingsreferanseListe = behandlingsreferanseResponseListe
        )
    }

    private fun hentStonadsendringerTilVedtak(stonadsendringListe: List<Stonadsendring>): List<StonadsendringDto> {
        val stonadsendringDtoListe = ArrayList<StonadsendringDto>()
        stonadsendringListe.forEach {
            val periodeListe = persistenceService.hentAllePerioderForStonadsendring(it.id)
            stonadsendringDtoListe.add(
                StonadsendringDto(
                    type = StonadType.valueOf(it.type),
                    sakId = it.sakId,
                    skyldnerId = it.skyldnerId,
                    kravhaverId = it.kravhaverId,
                    mottakerId = it.mottakerId,
                    indeksreguleringAar = it.indeksreguleringAar,
                    innkreving = Innkreving.valueOf(it.innkreving),
                    endring = it.endring,
                    omgjorVedtakId = it.omgjorVedtakId,
                    eksternReferanse = it.eksternReferanse,
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
            val periodeGrunnlagListe = persistenceService.hentAlleGrunnlagForPeriode(dto.id)
            periodeGrunnlagListe.forEach {
                val grunnlag = persistenceService.hentGrunnlag(it.grunnlag.id)
                grunnlagReferanseResponseListe.add(grunnlag.referanse)
            }
            periodeResponseListe.add(
                VedtakPeriodeDto(
                    fomDato = dto.fomDato,
                    tilDato = dto.tilDato,
                    belop = dto.belop,
                    valutakode = dto.valutakode?.trimEnd(),
                    resultatkode = dto.resultatkode,
                    delytelseId = dto.delytelseId,
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
            val engangsbelopGrunnlagListe = persistenceService.hentAlleGrunnlagForEngangsbelop(dto.id)
            engangsbelopGrunnlagListe.forEach {
                val grunnlag = persistenceService.hentGrunnlag(it.grunnlag.id)
                grunnlagReferanseResponseListe.add(grunnlag.referanse)
            }
            engangsbelopResponseListe.add(
                EngangsbelopDto(
                    type = EngangsbelopType.valueOf(dto.type),
                    sakId = dto.sakId,
                    skyldnerId = dto.skyldnerId,
                    kravhaverId = dto.kravhaverId,
                    mottakerId = dto.mottakerId,
                    belop = dto.belop,
                    valutakode = dto.valutakode,
                    resultatkode = dto.resultatkode,
                    innkreving = Innkreving.valueOf(dto.innkreving),
                    endring = dto.endring,
                    omgjorVedtakId = dto.omgjorVedtakId,
                    referanse = dto.referanse,
                    delytelseId = dto.delytelseId,
                    eksternReferanse = dto.eksternReferanse,
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
