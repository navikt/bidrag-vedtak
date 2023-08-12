package no.nav.bidrag.vedtak.service

import no.nav.bidrag.domain.enums.BehandlingsrefKilde
import no.nav.bidrag.domain.enums.EngangsbelopType
import no.nav.bidrag.domain.enums.Innkreving
import no.nav.bidrag.domain.enums.StonadType
import no.nav.bidrag.domain.enums.VedtakKilde
import no.nav.bidrag.domain.enums.VedtakType
import no.nav.bidrag.transport.behandling.vedtak.response.BehandlingsreferanseDto
import no.nav.bidrag.transport.behandling.vedtak.response.EngangsbelopDto
import no.nav.bidrag.transport.behandling.vedtak.response.GrunnlagDto
import no.nav.bidrag.transport.behandling.vedtak.response.StonadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettBehandlingsreferanseRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettEngangsbelopRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettGrunnlagRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettStonadsendringRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtakPeriodeRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtakRequestDto
import no.nav.bidrag.vedtak.SECURE_LOGGER
import no.nav.bidrag.vedtak.bo.EngangsbelopGrunnlagBo
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import no.nav.bidrag.vedtak.exception.custom.GrunnlagsdataManglerException
import no.nav.bidrag.vedtak.exception.custom.VedtaksdataMatcherIkkeException
import no.nav.bidrag.vedtak.persistence.entity.Engangsbelop
import no.nav.bidrag.vedtak.persistence.entity.EngangsbelopGrunnlagPK
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.PeriodeGrunnlagPK
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

    // Lister med generert db-id som skal brukes for å slette eventuelt eksisterende grunnlag ved oppdatering av vedtak
    val periodeIdGrunnlagSkalSlettesListe = mutableListOf<Int>()
    val engangsbelopIdGrunnlagSkalSlettesListe = mutableListOf<Int>()

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
    private fun opprettEngangsbelop(
        engangsbelopRequest: OpprettEngangsbelopRequestDto,
        vedtak: Vedtak,
        grunnlagIdRefMap: Map<String, Int>
    ): Engangsbelop {
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

        if (vedtakRequest.grunnlagListe.isEmpty()) {
            val feilmelding = "Grunnlagsdata mangler fra OppdaterVedtakRequest"
            LOGGER.error(feilmelding)
            SECURE_LOGGER.error("$feilmelding $vedtakRequest")
            throw GrunnlagsdataManglerException(feilmelding)
        }

        if (alleVedtaksdataMatcher(vedtakId, vedtakRequest)) {
            slettEventueltEksisterendeGrunnlag(vedtakId)
            oppdaterGrunnlag(vedtakId, vedtakRequest)
        } else {
            val feilmelding = "Innsendte data for oppdatering av vedtak matcher ikke med eksisterende vedtaksdata"
            LOGGER.error(feilmelding)
            SECURE_LOGGER.error("$feilmelding $vedtakRequest")
            throw VedtaksdataMatcherIkkeException(feilmelding)
        }

        return vedtakId
    }

    private fun alleVedtaksdataMatcher(vedtakId: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean {
        return vedtakMatcher(vedtakId, vedtakRequest) &&
            stonadsendringerOgPerioderMatcher(vedtakId, vedtakRequest) &&
            engangsbelopMatcher(vedtakId, vedtakRequest) &&
            behandlingsreferanserMatcher(vedtakId, vedtakRequest)
    }

    private fun vedtakMatcher(vedtakId: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean {
        val eksisterendeVedtak = persistenceService.hentVedtak(vedtakId)
        return vedtakRequest.kilde.toString() == eksisterendeVedtak.kilde &&
            vedtakRequest.type.toString() == eksisterendeVedtak.type &&
            vedtakRequest.opprettetAv == eksisterendeVedtak.opprettetAv &&
            vedtakRequest.opprettetAvNavn == eksisterendeVedtak.opprettetAvNavn &&
            vedtakRequest.vedtakTidspunkt.year == eksisterendeVedtak.vedtakTidspunkt.year &&
            vedtakRequest.vedtakTidspunkt.month == eksisterendeVedtak.vedtakTidspunkt.month &&
            vedtakRequest.vedtakTidspunkt.dayOfMonth == eksisterendeVedtak.vedtakTidspunkt.dayOfMonth &&
            vedtakRequest.vedtakTidspunkt.hour == eksisterendeVedtak.vedtakTidspunkt.hour &&
            vedtakRequest.vedtakTidspunkt.minute == eksisterendeVedtak.vedtakTidspunkt.minute &&
            vedtakRequest.vedtakTidspunkt.second == eksisterendeVedtak.vedtakTidspunkt.second &&
            vedtakRequest.enhetId == eksisterendeVedtak.enhetId &&
            vedtakRequest.utsattTilDato == eksisterendeVedtak.utsattTilDato
    }

    private fun stonadsendringerOgPerioderMatcher(vedtakId: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean {
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
            SECURE_LOGGER.error("Det er ulikt antall stønadsendringer i request for å oppdater vedtak og det som er lagret på vedtaket fra før. VedtakId $vedtakId")
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
            SECURE_LOGGER.error("Det er mismatch på minst én stønadsendring ved forsøk på å oppdatere vedtak $vedtakId")
            return false
        }

        // Sorterer listene likt for å kunne sammenligne perioder
        val sortertStonadsendringRequestListe = vedtakRequest.stonadsendringListe!!
            .sortedWith(compareBy({ it.type }, { it.skyldnerId }, { it.kravhaverId }, { it.sakId }))

        for ((i, stonadsendring) in eksisterendeStonadsendringListe.withIndex()) {
            if (!perioderMatcher(stonadsendring.id, sortertStonadsendringRequestListe[i])) {
                SECURE_LOGGER.error("Det er mismatch på minst én periode ved forsøk på å oppdatere vedtak $vedtakId, stønadsendring: ${stonadsendring.id}")
                return false
            }
        }
        return true
    }

    private fun perioderMatcher(stonadsendringId: Int, stonadsendringRequest: OpprettStonadsendringRequestDto): Boolean {
        val eksisterendePeriodeListe = persistenceService.hentAllePerioderForStonadsendring(stonadsendringId)

        val matchendeElementer = stonadsendringRequest.periodeListe
            .filter { periodeRequest ->
                eksisterendePeriodeListe.any {
                    periodeRequest.fomDato == it.fomDato &&
                        periodeRequest.tilDato == it.tilDato &&
                        periodeRequest.belop?.toInt() == it.belop?.toInt() &&
                        periodeRequest.valutakode == it.valutakode &&
                        periodeRequest.resultatkode == it.resultatkode &&
                        periodeRequest.delytelseId == it.delytelseId
                }
            }

        if (matchendeElementer.size == eksisterendePeriodeListe.size) {
            eksisterendePeriodeListe.forEach {
                periodeIdGrunnlagSkalSlettesListe.add(it.id)
            }
        }

        return matchendeElementer.size == eksisterendePeriodeListe.size
    }

    private fun engangsbelopMatcher(vedtakId: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean {
        val eksisterendeEngangsbelopListe = persistenceService.hentAlleEngangsbelopForVedtak(vedtakId)

        // vedtakRequest.engangsbelopListe kan være null, eksisterendeEngangsbelopListe kan ikke være null,
        // bare emptyList
        if (vedtakRequest.engangsbelopListe.isNullOrEmpty()) {
            return eksisterendeEngangsbelopListe.isEmpty()
        }

        // Sjekker om det er lagret like mange engangsbeløp som det ligger i oppdaterVedtak-requesten
        if (vedtakRequest.engangsbelopListe?.size != eksisterendeEngangsbelopListe.size) {
            SECURE_LOGGER.error("Det er ulikt antall engangsbeløp i request for å oppdater vedtak og det som er lagret på vedtaket fra før. VedtakId $vedtakId")
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
                        engangsbelopRequest.belop?.toInt() == it.belop?.toInt() &&
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

        if (matchendeElementer.size == eksisterendeEngangsbelopListe.size) {
            eksisterendeEngangsbelopListe.forEach {
                engangsbelopIdGrunnlagSkalSlettesListe.add(it.id)
            }
        }

        return matchendeElementer.size == eksisterendeEngangsbelopListe.size
    }

    private fun behandlingsreferanserMatcher(vedtakId: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean {
        val eksisterendeBehandlingsreferanseListe = persistenceService.hentAlleBehandlingsreferanserForVedtak(vedtakId)

        // vedtakRequest.engangsbelopListe kan være null, eksisterendeEngangsbelopListe kan ikke være null,
        // bare emptyList
        if (vedtakRequest.behandlingsreferanseListe.isNullOrEmpty()) {
            return eksisterendeBehandlingsreferanseListe.isEmpty()
        }

        // Sjekker om det er lagret like mange behandlinmgsreferanser som det ligger i oppdaterVedtak-requesten
        if (vedtakRequest.behandlingsreferanseListe?.size != eksisterendeBehandlingsreferanseListe.size) {
            SECURE_LOGGER.error("Det er ulikt antall behandlingsreferanser i request for å oppdater vedtak og det som er lagret på vedtaket fra før. VedtakId $vedtakId")
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

    private fun slettEventueltEksisterendeGrunnlag(vedtakId: Int) {
        // slett fra PeriodeGrunnlag
        if (periodeIdGrunnlagSkalSlettesListe.isNotEmpty()) {
            periodeIdGrunnlagSkalSlettesListe.forEach { periodeId ->
                val periodeGrunnlag = persistenceService.hentAlleGrunnlagForPeriode(periodeId)
                periodeGrunnlag.forEach {
                    persistenceService.periodeGrunnlagRepository.deleteById(PeriodeGrunnlagPK(periodeId, it.grunnlag.id))
                }
            }
        }

        // slett fra EngangsbelopGrunnlag
        if (engangsbelopIdGrunnlagSkalSlettesListe.isNotEmpty()) {
            engangsbelopIdGrunnlagSkalSlettesListe.forEach { engangsbelopId ->
                val engangsbelopGrunnlag = persistenceService.hentAlleGrunnlagForEngangsbelop(engangsbelopId)
                engangsbelopGrunnlag.forEach {
                    persistenceService.engangsbelopGrunnlagRepository.deleteById(EngangsbelopGrunnlagPK(engangsbelopId, it.grunnlag.id))
                }
                // tester på github feiler uten denne, ikke spør...
                val eg = persistenceService.engangsbelopGrunnlagRepository.hentAlleGrunnlagForEngangsbelop(engangsbelopId)
            }
        }

        // slett fra Grunnlag
        persistenceService.slettAlleGrunnlagForVedtak(vedtakId)

        // Initialiserer lister
        periodeIdGrunnlagSkalSlettesListe.clear()
        engangsbelopIdGrunnlagSkalSlettesListe.clear()
    }

    private fun oppdaterGrunnlag(vedtakId: Int, vedtakRequest: OpprettVedtakRequestDto) {
        val vedtak = persistenceService.hentVedtak(vedtakId)

        val grunnlagIdRefMap = mutableMapOf<String, Int>()

        // Lagrer grunnlag
        vedtakRequest.grunnlagListe.forEach {
            val opprettetGrunnlagId = opprettGrunnlag(it, vedtak)
            grunnlagIdRefMap[it.referanse] = opprettetGrunnlagId.id
        }

        // oppdaterer PeriodeGrunnlag
        val eksisterendeStonadsendringListe = persistenceService.hentAlleStonadsendringerForVedtak(vedtakId)

        vedtakRequest.stonadsendringListe?.forEach { stonadsendringRequest ->
            // matcher mot eksisterende stønadsendringer for å finne stonadsendringId for igjen å finne perioder som skal brukes
            // til å oppdatere PeriodeGrunnlag
            val stonadsendringId = finnEksisterendeStonadsendringId(stonadsendringRequest, eksisterendeStonadsendringListe)
            val eksisterendePeriodeListe = persistenceService.hentAllePerioderForStonadsendring(stonadsendringId)

            stonadsendringRequest.periodeListe.forEach { periode ->
                // matcher mot eksisterende perioder for å finne periodeId for å oppdatere PeriodeGrunnlag
                val periodeId = finnEksisterendePeriodeId(periode, eksisterendePeriodeListe)
                oppdaterPeriodeGrunnlag(periode, periodeId, grunnlagIdRefMap)
            }
        }

        // oppdaterer EngangsbelopGrunnlag
        val eksisterendeEngangsbelopListe = persistenceService.hentAlleEngangsbelopForVedtak(vedtakId)
        vedtakRequest.engangsbelopListe?.forEach { engangsbelop ->
            // matcher mot eksisterende engangsbeløp for å finne engangsbeløpId for igjen å oppdatere EngangsbelopGrunnlag
            val engangsbelopId = finnTilhørendeEngangsbelopId(engangsbelop, eksisterendeEngangsbelopListe)
            oppdaterEngangsbelopGrunnlag(engangsbelop, engangsbelopId, grunnlagIdRefMap)
        }
    }

    private fun finnEksisterendeStonadsendringId(
        stonadsendringrequest: OpprettStonadsendringRequestDto,
        eksisterendeStonadsendringListe: List<Stonadsendring>
    ): Int {
        val matchendeEksisterendeStonadsendring = eksisterendeStonadsendringListe
            .filter { stonadsendring ->
                eksisterendeStonadsendringListe.any {
                    stonadsendring.type == stonadsendringrequest.type.toString() &&
                        stonadsendring.sakId == stonadsendringrequest.sakId &&
                        stonadsendring.skyldnerId == stonadsendringrequest.skyldnerId &&
                        stonadsendring.kravhaverId == stonadsendringrequest.kravhaverId &&
                        stonadsendring.mottakerId == stonadsendringrequest.mottakerId &&
                        stonadsendring.indeksreguleringAar == stonadsendringrequest.indeksreguleringAar &&
                        stonadsendring.innkreving == stonadsendringrequest.innkreving.toString() &&
                        stonadsendring.endring == stonadsendringrequest.endring &&
                        stonadsendring.omgjorVedtakId == stonadsendringrequest.omgjorVedtakId &&
                        stonadsendring.eksternReferanse == stonadsendringrequest.eksternReferanse
                }
            }

        if (matchendeEksisterendeStonadsendring.size != 1) {
            SECURE_LOGGER.error("Det er mismatch på antall matchende stønadsendringer $stonadsendringrequest")
            throw VedtaksdataMatcherIkkeException("Det er mismatch på antall matchende stønadsendringer $stonadsendringrequest")
        }
        return matchendeEksisterendeStonadsendring.first().id
    }

    private fun finnEksisterendePeriodeId(periodeRequest: OpprettVedtakPeriodeRequestDto, eksisterendePeriodeListe: List<Periode>): Int {
        val matchendeEksisterendePeriode = eksisterendePeriodeListe
            .filter { eksisterendePeriode ->
                eksisterendePeriodeListe.any {
                    eksisterendePeriode.fomDato == periodeRequest.fomDato &&
                        eksisterendePeriode.tilDato == periodeRequest.tilDato &&
                        eksisterendePeriode.belop?.toInt() == periodeRequest.belop?.toInt() &&
                        eksisterendePeriode.valutakode == periodeRequest.valutakode &&
                        eksisterendePeriode.resultatkode == periodeRequest.resultatkode &&
                        eksisterendePeriode.delytelseId == periodeRequest.delytelseId
                }
            }

        if (matchendeEksisterendePeriode.size != 1) {
            SECURE_LOGGER.error("Det er mismatch på antall matchende perioder for stønadsendring $periodeRequest")
            throw VedtaksdataMatcherIkkeException("Det er mismatch på antall matchende stønadsendringer $periodeRequest")
        }
        return matchendeEksisterendePeriode.first().id
    }

    // Oppdater periode
    private fun oppdaterPeriodeGrunnlag(periodeRequest: OpprettVedtakPeriodeRequestDto, periodeId: Int, grunnlagIdRefMap: Map<String, Int>) {
        // PeriodeGrunnlag
        periodeRequest.grunnlagReferanseListe.forEach {
            val grunnlagId = grunnlagIdRefMap.getOrDefault(it, 0)
            if (grunnlagId == 0) {
                val feilmelding = "grunnlagReferanse $it ikke funnet i intern mappingtabell"
                LOGGER.error(feilmelding)
                throw IllegalArgumentException(feilmelding)
            } else {
                val periodeGrunnlagBo = PeriodeGrunnlagBo(
                    periodeId = periodeId,
                    grunnlagId = grunnlagId
                )
                persistenceService.opprettPeriodeGrunnlag(periodeGrunnlagBo)
            }
        }
    }

    // Finner generert db-id for eksisterende stønadsendring
    private fun finnTilhørendeEngangsbelopId(
        engangsbelopRequest: OpprettEngangsbelopRequestDto,
        eksisterendeEngangsbelopListe: List<Engangsbelop>
    ): Int {
        val matchendeEksisterendeEngangsbelop = eksisterendeEngangsbelopListe
            .filter { engangsbelop ->
                eksisterendeEngangsbelopListe.any {
                    engangsbelop.type == engangsbelopRequest.type.toString() &&
                        engangsbelop.sakId == engangsbelopRequest.sakId &&
                        engangsbelop.skyldnerId == engangsbelopRequest.skyldnerId &&
                        engangsbelop.kravhaverId == engangsbelopRequest.kravhaverId &&
                        engangsbelop.mottakerId == engangsbelopRequest.mottakerId &&
                        engangsbelop.belop?.toInt() == engangsbelopRequest.belop?.toInt() &&
                        engangsbelop.valutakode == engangsbelopRequest.valutakode &&
                        engangsbelop.resultatkode == engangsbelopRequest.resultatkode &&
                        engangsbelop.innkreving == engangsbelopRequest.innkreving.toString() &&
                        engangsbelop.endring == engangsbelopRequest.endring &&
                        engangsbelop.omgjorVedtakId == engangsbelopRequest.omgjorVedtakId &&
                        engangsbelop.referanse == engangsbelopRequest.referanse &&
                        engangsbelop.delytelseId == engangsbelopRequest.delytelseId &&
                        engangsbelop.eksternReferanse == engangsbelopRequest.eksternReferanse
                }
            }
        if (matchendeEksisterendeEngangsbelop.size != 1) {
            SECURE_LOGGER.error("Det er mismatch på antall matchende engangsbeløp $engangsbelopRequest")
            throw VedtaksdataMatcherIkkeException("Det er mismatch på antall matchende engangsbeløp $engangsbelopRequest")
        }
        return matchendeEksisterendeEngangsbelop.first().id
    }

    // Opprett EngangsbelopGrunnlag
    private fun oppdaterEngangsbelopGrunnlag(
        engangsbelopRequest: OpprettEngangsbelopRequestDto,
        engangsbelopId: Int,
        grunnlagIdRefMap: Map<String, Int>
    ) {
        // EngangsbelopGrunnlag
        engangsbelopRequest.grunnlagReferanseListe.forEach {
            val grunnlagId = grunnlagIdRefMap.getOrDefault(it, 0)
            if (grunnlagId == 0) {
                val feilmelding = "grunnlagReferanse $it ikke funnet i intern mappingtabell"
                LOGGER.error(feilmelding)
                throw IllegalArgumentException(feilmelding)
            } else {
                val engangsbelopGrunnlagBo = EngangsbelopGrunnlagBo(
                    engangsbelopId = engangsbelopId,
                    grunnlagId = grunnlagId
                )
                persistenceService.opprettEngangsbelopGrunnlag(engangsbelopGrunnlagBo)
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(VedtakService::class.java)
    }
}
