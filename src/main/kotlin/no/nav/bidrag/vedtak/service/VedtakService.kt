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
import no.nav.bidrag.vedtak.exception.custom.VedtaksdataMatcherIkkeException
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
                opprettetAvNavn = vedtak.opprettetAvNavn,
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

    fun oppdaterVedtak(vedtakId: Int, vedtakRequest: OpprettVedtakRequestDto): Int {
//        val eksisterendeVedtak = hentVedtak(vedtakId)

        if (alleVedtaksdataMatcher(vedtakId, vedtakRequest)) {


        } else
            throw VedtaksdataMatcherIkkeException("Innsendte data for oppdatering av vedtak matcher ikke med eksisterende vedtak. VedtakId: $vedtakId")



        val vedtaksdataMatcher = true

        if (!vedtaksdataMatcher) {
        }

        return vedtakId
    }


    fun alleVedtaksdataMatcher(vedtakId: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean {
        return vedtakMatcher(vedtakId, vedtakRequest) &&
                stonadsendringerOgPerioderMatcher(vedtakId, vedtakRequest) &&
                engangsbelopMatcher(vedtakId, vedtakRequest) &&
                behandlingsreferanserMatcher(vedtakId, vedtakRequest)
    }

    fun vedtakMatcher(vedtakId: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean {
        val eksisterendeVedtak = persistenceService.hentVedtak(vedtakId)
        return vedtakRequest.kilde.toString() == eksisterendeVedtak.kilde &&
                vedtakRequest.type.toString() == eksisterendeVedtak.type &&
                vedtakRequest.opprettetAv == eksisterendeVedtak.opprettetAv &&
                vedtakRequest.opprettetAvNavn == eksisterendeVedtak.opprettetAvNavn &&
                vedtakRequest.vedtakTidspunkt == eksisterendeVedtak.vedtakTidspunkt &&
                vedtakRequest.enhetId == eksisterendeVedtak.enhetId &&
                vedtakRequest.utsattTilDato == eksisterendeVedtak.utsattTilDato
    }

    fun stonadsendringerOgPerioderMatcher(vedtakId: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean {

        // Sorterer begge listene likt
        val eksisterendeStonadsendringListe = persistenceService.hentAlleStonadsendringerForVedtak(vedtakId)
                .sortedWith(compareBy({ it.type }, { it.skyldnerId }, { it.kravhaverId }, { it.sakId }))

        // vedtakRequest.stonadsendringListe kan være null, eksisterendeStonadsendringListe kan ikke være null,
        // bare emptyList
        if (vedtakRequest.stonadsendringListe.isNullOrEmpty()) {
            return eksisterendeStonadsendringListe.isEmpty()
        }

        // Sjekker om det er lagret like mange stønadsendringer som det ligger i oppdaterVedtak-requesten
        if (vedtakRequest.stonadsendringListe?.size != eksisterendeStonadsendringListe.size) {
            return false
        }

        // Teller antall forekomster som matcher. Hvis antallet er lavere enn antall stønadsendringer
        // som ligger på vedtaket fra før så feilmeldes det
        val matchendeElementer = vedtakRequest.stonadsendringListe!!
                .filter { stonadsendringRequest ->
                    eksisterendeStonadsendringListe.any {
                        stonadsendringRequest.type.toString() == it.type &&
                                stonadsendringRequest.sakId == it.sakId &&
                                stonadsendringRequest.skyldnerId == it.skyldnerId &&
                                stonadsendringRequest.kravhaverId == it.kravhaverId &&
                                stonadsendringRequest.mottakerId == it.mottakerId &&
                                stonadsendringRequest.indeksreguleringAar == it.indeksreguleringAar &&
                                stonadsendringRequest.innkreving.toString() == it.innkreving &&
                                stonadsendringRequest.endring == it.endring &&
                                stonadsendringRequest.omgjorVedtakId == it.omgjorVedtakId &&
                                stonadsendringRequest.eksternReferanse == it.eksternReferanse
                    }
                }
        if (matchendeElementer.size != eksisterendeStonadsendringListe.size) {
            return false
        }

        // Sorterer listene likt for å kunne sammenligne perioder
        val sortertStonadsendringRequestListe = vedtakRequest.stonadsendringListe!!
                .sortedWith(compareBy({ it.type }, { it.skyldnerId }, { it.kravhaverId }, { it.sakId }))

        for ((i, stonadsendring) in eksisterendeStonadsendringListe.withIndex()) {
            if (!perioderMatcher(stonadsendring.id, sortertStonadsendringRequestListe[i])) {
                return false
            }
        }
        return true
    }

    fun perioderMatcher(stonadsendringId: Int, stonadsendringRequest: OpprettStonadsendringRequestDto): Boolean {

        val eksisterendePeriodeListe = persistenceService.hentAllePerioderForStonadsendring(stonadsendringId)

        val matchendeElementer = stonadsendringRequest.periodeListe
                .filter { periodeRequest -> eksisterendePeriodeListe.any {
                    periodeRequest.fomDato == it.fomDato &&
                    periodeRequest.tilDato == it.tilDato &&
                    periodeRequest.belop == it.belop &&
                    periodeRequest.valutakode == it.valutakode &&
                    periodeRequest.resultatkode == it.resultatkode &&
                    periodeRequest.delytelseId == it.delytelseId
                } }

        return matchendeElementer.size == eksisterendePeriodeListe.size
    }


    fun engangsbelopMatcher(vedtakId: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean {

        val eksisterendeEngangsbelopListe = persistenceService.hentAlleEngangsbelopForVedtak(vedtakId)

        // vedtakRequest.engangsbelopListe kan være null, eksisterendeEngangsbelopListe kan ikke være null,
        // bare emptyList
        if (vedtakRequest.engangsbelopListe.isNullOrEmpty()) {
            return eksisterendeEngangsbelopListe.isEmpty()
        }

        // Sjekker om det er lagret like mange engangsbeløp som det ligger i oppdaterVedtak-requesten
        if (vedtakRequest.engangsbelopListe?.size != eksisterendeEngangsbelopListe.size) {
            return false
        }

        // Teller antall forekomster som matcher. Hvis antallet er lavere enn antall engangsbeløp
        // som ligger på vedtaket fra før så feilmeldes det
        val matchendeElementer = vedtakRequest.engangsbelopListe!!
                .filter { engangsbelopRequest ->
                    eksisterendeEngangsbelopListe.any {
                        engangsbelopRequest.type.toString() == it.type &&
                                engangsbelopRequest.sakId == it.sakId &&
                                engangsbelopRequest.skyldnerId == it.skyldnerId &&
                                engangsbelopRequest.kravhaverId == it.kravhaverId &&
                                engangsbelopRequest.mottakerId == it.mottakerId &&
                                engangsbelopRequest.belop == it.belop &&
                                engangsbelopRequest.valutakode == it.valutakode &&
                                engangsbelopRequest.resultatkode == it.resultatkode &&
                                engangsbelopRequest.innkreving.toString() == it.innkreving &&
                                engangsbelopRequest.endring == it.endring &&
                                engangsbelopRequest.omgjorVedtakId == it.omgjorVedtakId &&
                                engangsbelopRequest.referanse == it.referanse &&
                                engangsbelopRequest.delytelseId == it.delytelseId &&
                                engangsbelopRequest.eksternReferanse == it.eksternReferanse
                    }
                }
        return matchendeElementer.size == eksisterendeEngangsbelopListe.size
    }

    fun behandlingsreferanserMatcher(vedtakId: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean {

        val eksisterendeBehandlingsreferanseListe = persistenceService.hentAlleBehandlingsreferanserForVedtak(vedtakId)

        // vedtakRequest.engangsbelopListe kan være null, eksisterendeEngangsbelopListe kan ikke være null,
        // bare emptyList
        if (vedtakRequest.behandlingsreferanseListe.isNullOrEmpty()) {
            return eksisterendeBehandlingsreferanseListe.isEmpty()
        }

        // Sjekker om det er lagret like mange behandlinmgsreferanser som det ligger i oppdaterVedtak-requesten
        if (vedtakRequest.behandlingsreferanseListe?.size != eksisterendeBehandlingsreferanseListe.size) {
            return false
        }

        // Teller antall forekomster som matcher. Hvis antallet er lavere enn antall engangsbeløp
        // som ligger på vedtaket fra før så feilmeldes det
        val matchendeElementer = vedtakRequest.behandlingsreferanseListe!!
                .filter { behandlingsreferanseRequestListe ->
                    eksisterendeBehandlingsreferanseListe.any {
                        behandlingsreferanseRequestListe.kilde.toString() == it.kilde &&
                        behandlingsreferanseRequestListe.referanse == it.referanse
                    }
                }
        return matchendeElementer.size == eksisterendeBehandlingsreferanseListe.size

    }


    companion object {
        private val LOGGER = LoggerFactory.getLogger(VedtakService::class.java)
    }
}
