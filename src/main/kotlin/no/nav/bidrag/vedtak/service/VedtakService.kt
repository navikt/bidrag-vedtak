package no.nav.bidrag.vedtak.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.bidrag.commons.security.utils.TokenUtils
import no.nav.bidrag.commons.service.organisasjon.SaksbehandlernavnProvider
import no.nav.bidrag.commons.util.IdentUtils
import no.nav.bidrag.domene.enums.vedtak.BehandlingsrefKilde
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Engangsbeløptype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.VedtaksforslagStatus
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.organisasjon.Enhetsnummer
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.domene.util.trimToNull
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.vedtak.request.HentVedtakForStønadRequest
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettBehandlingsreferanseRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettEngangsbeløpRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettGrunnlagRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettPeriodeRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettStønadsendringRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtakRequestDto
import no.nav.bidrag.transport.behandling.vedtak.response.BehandlingsreferanseDto
import no.nav.bidrag.transport.behandling.vedtak.response.EngangsbeløpDto
import no.nav.bidrag.transport.behandling.vedtak.response.HentVedtakForStønadResponse
import no.nav.bidrag.transport.behandling.vedtak.response.OpprettVedtakResponseDto
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakForStønad
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import no.nav.bidrag.vedtak.SECURE_LOGGER
import no.nav.bidrag.vedtak.bo.EngangsbeløpGrunnlagBo
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import no.nav.bidrag.vedtak.bo.StønadsendringGrunnlagBo
import no.nav.bidrag.vedtak.exception.custom.ConflictException
import no.nav.bidrag.vedtak.exception.custom.GrunnlagsdataManglerException
import no.nav.bidrag.vedtak.exception.custom.PreconditionFailedException
import no.nav.bidrag.vedtak.exception.custom.VedtaksdataMatcherIkkeException
import no.nav.bidrag.vedtak.exception.custom.duplikateReferanserEngangsbeløp
import no.nav.bidrag.vedtak.exception.custom.manglerOpprettetAv
import no.nav.bidrag.vedtak.exception.custom.referanseTilPåklagetEngangsbeløpMangler
import no.nav.bidrag.vedtak.persistence.entity.Engangsbeløp
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.Stønadsendring
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import no.nav.bidrag.vedtak.persistence.entity.toBehandlingsreferanseEntity
import no.nav.bidrag.vedtak.persistence.entity.toEngangsbeløpEntity
import no.nav.bidrag.vedtak.persistence.entity.toGrunnlagDto
import no.nav.bidrag.vedtak.persistence.entity.toGrunnlagEntity
import no.nav.bidrag.vedtak.persistence.entity.toPeriodeEntity
import no.nav.bidrag.vedtak.persistence.entity.toStønadsendringEntity
import no.nav.bidrag.vedtak.persistence.entity.toVedtakEntity
import no.nav.bidrag.vedtak.util.VedtakUtil.Companion.tilJson
import org.hibernate.exception.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

data class VedtakConflictResponse(val vedtaksid: Int?)

@Service
@Transactional
class VedtakService(
    val persistenceService: PersistenceService,
    val hendelserService: HendelserService,
    private val meterRegistry: MeterRegistry,
    private val identUtils: IdentUtils,
) {

    private val opprettVedtakCounterName = "opprett_vedtak"
    private val oppdaterVedtakCounterName = "oppdater_vedtak"

    // Lister med generert db-id som skal brukes for å slette eventuelt eksisterende grunnlag ved oppdatering av vedtak
    val stønadsendringsidGrunnlagSkalSlettesListe = mutableListOf<Int>()
    val periodeidGrunnlagSkalSlettesListe = mutableListOf<Int>()
    val engangsbeløpsidGrunnlagSkalSlettesListe = mutableListOf<Int>()

    fun hentAlleVedtaksforslagIder(limit: Int): List<Int> = persistenceService.hentAlleVedtaksforslagIder(limit)

    // Opprett vedtak (alle tabeller)
    fun opprettVedtak(vedtakRequest: OpprettVedtakRequestDto, vedtaksforslag: Boolean): OpprettVedtakResponseDto {
        // Hent saksbehandlerident (opprettetAv) og kildeapplikasjon fra token. + Navn på saksbehandler (opprettetAvNavn) fra bidrag-organisasjon.
        val opprettetAv =
            vedtakRequest.opprettetAv.trimToNull()
                ?: TokenUtils.hentSaksbehandlerIdent()
                ?: TokenUtils.hentApplikasjonsnavn()
                ?: vedtakRequest.manglerOpprettetAv()

        val opprettetAvNavn = SaksbehandlernavnProvider.hentSaksbehandlernavn(opprettetAv)
        val kildeapplikasjon = TokenUtils.hentApplikasjonsnavn() ?: "UKJENT"

        if (vedtaksforslag && vedtakRequest.vedtakstidspunkt != null) {
            throw IllegalArgumentException("Vedtakstidspunkt kan ikke være angitt ved opprettelse av vedtaksforslag")
        }

        val stønadsendringerMedAngittSisteVedtaksidListe = vedtakRequest.stønadsendringListe.filter { it.sisteVedtaksid != null }
        stønadsendringerMedAngittSisteVedtaksidListe.forEach { stønad ->
            if (!validerAtSisteVedtaksidErOk(
                    type = stønad.type,
                    saksnummer = stønad.sak,
                    skyldner = stønad.skyldner,
                    kravhaver = stønad.kravhaver,
                    sisteVedtaksid = stønad.sisteVedtaksid,
                )
            ) {
                throw PreconditionFailedException("Angitt sisteVedtaksid er ikke lik lagret siste vedtaksid")
            }
        }

        val vedtakstidspunkt = if (vedtaksforslag) null else vedtakRequest.vedtakstidspunkt ?: LocalDateTime.now()

        // sjekk om alle referanser for engangsbeløp er unike. Forekomster med null i referanse utelukkes i sjekken.
        if (vedtakRequest.engangsbeløpListe.isNotEmpty()) {
            if (duplikateReferanser(vedtakRequest.engangsbeløpListe.filter { it.referanse != null })) {
                // Kaster exception hvis det er duplikate referanser
                vedtakRequest.duplikateReferanserEngangsbeløp()
            } else {
                // Kaster exception hvis det er et klagevedtak og det mangler referanse til engangsbeløp.
                if (vedtakRequest.engangsbeløpListe.any { it.omgjørVedtakId != null && it.referanse == null }) {
                    vedtakRequest.referanseTilPåklagetEngangsbeløpMangler()
                }
            }
        }

        // Opprett vedtak
        val opprettetVedtak = try {
            persistenceService.opprettVedtak(vedtakRequest.toVedtakEntity(opprettetAv, opprettetAvNavn, kildeapplikasjon, vedtakstidspunkt))
        } catch (e: DataIntegrityViolationException) {
            behandleDataIntegrityException(e, vedtakRequest)
        } catch (e: Exception) {
            // Only handle other unexpected exceptions
            LOGGER.error("Uventet feil ved lagring av vedtak")
            SECURE_LOGGER.error("Uventet feil ved lagring av vedtak: ${e.message}", e)
            throw e
        }

        val grunnlagIdRefMap = mutableMapOf<String, Int>()

        val engangsbeløpReferanseListe = mutableListOf<String>()

        // Grunnlag
        vedtakRequest.grunnlagListe.forEach {
            val opprettetGrunnlagId = opprettGrunnlag(it, opprettetVedtak)
            grunnlagIdRefMap[it.referanse] = opprettetGrunnlagId.id
        }

        // Stønadsendring
        vedtakRequest.stønadsendringListe.forEach { opprettStønadsendring(it, opprettetVedtak, grunnlagIdRefMap) }

        // Engangsbeløp
        vedtakRequest.engangsbeløpListe.forEach {
            engangsbeløpReferanseListe.add(opprettEngangsbeløp(it, opprettetVedtak, grunnlagIdRefMap).referanse)
        }

        // Behandlingsreferanse
        vedtakRequest.behandlingsreferanseListe.forEach { opprettBehandlingsreferanse(it, opprettetVedtak) }

        // Hent lagret vedtak for bruk til å lage hendelse
        val vedtakDto = hentVedtak(opprettetVedtak.id)

        if ((vedtakRequest.stønadsendringListe.isNotEmpty() || vedtakRequest.engangsbeløpListe.isNotEmpty()) && !vedtaksforslag) {
            hendelserService.opprettHendelseVedtak(
                vedtakDto = vedtakDto,
                vedtakId = opprettetVedtak.id,
            )
        }

        if (vedtaksforslag) {
            hendelserService.opprettHendelseVedtaksforslag(
                status = VedtaksforslagStatus.OPPRETTET,
                request = vedtakRequest,
                vedtakId = opprettetVedtak.id,
                saksnummer = vedtakRequest.stønadsendringListe.firstOrNull()?.sak,
            )
        }

        measureVedtak(opprettVedtakCounterName, vedtakRequest)
        return OpprettVedtakResponseDto(opprettetVedtak.id, engangsbeløpReferanseListe)
    }

    private fun behandleDataIntegrityException(e: DataIntegrityViolationException, vedtakRequest: OpprettVedtakRequestDto): Nothing {
        if (e.cause is ConstraintViolationException) {
            val unikReferanse = vedtakRequest.unikReferanse
            val psqlException = (e.cause as ConstraintViolationException).sqlException
            // 23505 betyr unique violation i postgres
            if (unikReferanse != null && psqlException.sqlState == "23505") {
                val vedtaksid = persistenceService.hentVedtakForUnikReferanseEgenTransaksjon(unikReferanse)?.id

                if (vedtaksid != null) {
                    LOGGER.error(
                        "Feil ved lagring av vedtak. Det finnes allerede et vedtak unik referansen ${vedtakRequest.unikReferanse} med vedtaksid $vedtaksid",
                    )
                    SECURE_LOGGER.error(
                        "Feil ved lagring av vedtak. Det finnes allerede et vedtak med unik referansen ${vedtakRequest.unikReferanse}. " +
                            "Id: $vedtaksid. Request: $vedtakRequest",
                    )
                    throw ConflictException("Et vedtak med angitt unikReferanse finnes allerede", VedtakConflictResponse(vedtaksid))
                }
            }
        }
        LOGGER.error("Uventet feil ved lagring av vedtak")
        SECURE_LOGGER.error("Uventet feil ved lagring av vedtak: ${e.message}", e)
        throw e
    }

    // Opprett grunnlag
    private fun opprettGrunnlag(grunnlagRequest: OpprettGrunnlagRequestDto, vedtak: Vedtak) =
        persistenceService.opprettGrunnlag(grunnlagRequest.toGrunnlagEntity(vedtak))

    // Opprett stønadsendring
    private fun opprettStønadsendring(stønadsendringRequest: OpprettStønadsendringRequestDto, vedtak: Vedtak, grunnlagIdRefMap: Map<String, Int>) {
        val opprettetStønadsendring = persistenceService.opprettStønadsendring(stønadsendringRequest.toStønadsendringEntity(vedtak))

        // StønadsendringGrunnlag
        stønadsendringRequest.grunnlagReferanseListe.forEach {
            val grunnlagId = grunnlagIdRefMap.getOrDefault(it, 0)
            if (grunnlagId == 0) {
                val feilmelding = "grunnlagReferanse $it ikke funnet i intern mappingtabell"
                LOGGER.error(feilmelding)
                throw IllegalArgumentException(feilmelding)
            } else {
                persistenceService.opprettStønadsendringGrunnlag(StønadsendringGrunnlagBo(opprettetStønadsendring.id, grunnlagId))
            }
        }

        // Periode
        stønadsendringRequest.periodeListe.forEach { opprettPeriode(it, opprettetStønadsendring, grunnlagIdRefMap) }
    }

    // Opprett Engangsbeløp
    private fun opprettEngangsbeløp(
        engangsbeløpRequest: OpprettEngangsbeløpRequestDto,
        vedtak: Vedtak,
        grunnlagIdRefMap: Map<String, Int>,
    ): Engangsbeløp {
        // Hvis referanse ikke er angitt så blir det generert en referanse. Den må være unik innenfor vedtaket.
        val referanse = engangsbeløpRequest.referanse ?: genererUnikReferanse(vedtak.id)
        val opprettetEngangsbeløp = persistenceService.opprettEngangsbeløp(engangsbeløpRequest.toEngangsbeløpEntity(vedtak, referanse))

        // EngangsbeløpGrunnlag
        engangsbeløpRequest.grunnlagReferanseListe.forEach {
            val grunnlagId = grunnlagIdRefMap.getOrDefault(it, 0)
            if (grunnlagId == 0) {
                val feilmelding = "grunnlagReferanse $it ikke funnet i intern mappingtabell"
                LOGGER.error(feilmelding)
                throw IllegalArgumentException(feilmelding)
            } else {
                persistenceService.opprettEngangsbeløpGrunnlag(EngangsbeløpGrunnlagBo(opprettetEngangsbeløp.id, grunnlagId))
            }
        }
        return opprettetEngangsbeløp
    }

    // Opprett periode
    private fun opprettPeriode(periodeRequest: OpprettPeriodeRequestDto, stønadsendring: Stønadsendring, grunnlagIdRefMap: Map<String, Int>) {
        val opprettetPeriode = persistenceService.opprettPeriode(periodeRequest.toPeriodeEntity(stønadsendring))

        // PeriodeGrunnlag
        periodeRequest.grunnlagReferanseListe.forEach {
            val grunnlagId = grunnlagIdRefMap.getOrDefault(it, 0)
            if (grunnlagId == 0) {
                val feilmelding = "grunnlagReferanse $it ikke funnet i intern mappingtabell"
                LOGGER.error(feilmelding)
                throw IllegalArgumentException(feilmelding)
            } else {
                val periodeGrunnlagBo = PeriodeGrunnlagBo(
                    periodeid = opprettetPeriode.id,
                    grunnlagsid = grunnlagId,
                )
                persistenceService.opprettPeriodeGrunnlag(periodeGrunnlagBo)
            }
        }
    }

    // Opprett behandlingsreferanse
    private fun opprettBehandlingsreferanse(behandlingsreferanseRequest: OpprettBehandlingsreferanseRequestDto, vedtak: Vedtak) =
        persistenceService.opprettBehandlingsreferanse(
            behandlingsreferanseRequest.toBehandlingsreferanseEntity(vedtak),
        )

    // Hent vedtaksdata
    fun hentVedtak(vedtaksid: Int): VedtakDto {
        val vedtak = persistenceService.hentVedtak(vedtaksid)
        val grunnlagDtoListe = ArrayList<GrunnlagDto>()
        val grunnlagListe = persistenceService.hentAlleGrunnlagForVedtak(vedtak.id)
        grunnlagListe.forEach {
            grunnlagDtoListe.add(it.toGrunnlagDto())
        }
        val stønadsendringListe = persistenceService.hentAlleStønadsendringerForVedtak(vedtak.id)
        val engangsbeløpListe = persistenceService.hentAlleEngangsbeløpForVedtak(vedtak.id)
        val behandlingsreferanseListe = persistenceService.hentAlleBehandlingsreferanserForVedtak(vedtak.id)
        val behandlingsreferanseResponseListe = ArrayList<BehandlingsreferanseDto>()
        behandlingsreferanseListe.forEach {
            behandlingsreferanseResponseListe.add(
                BehandlingsreferanseDto(BehandlingsrefKilde.valueOf(it.kilde), it.referanse),
            )
        }

        return VedtakDto(
            vedtaksid = vedtak.id.toLong(),
            kilde = Vedtakskilde.valueOf(vedtak.kilde),
            type = Vedtakstype.valueOf(vedtak.type),
            opprettetAv = vedtak.opprettetAv,
            opprettetAvNavn = vedtak.opprettetAvNavn,
            kildeapplikasjon = vedtak.kildeapplikasjon,
            vedtakstidspunkt = vedtak.vedtakstidspunkt,
            unikReferanse = vedtak.unikReferanse,
            enhetsnummer = if (vedtak.enhetsnummer != null) Enhetsnummer(vedtak.enhetsnummer) else null,
            opprettetTidspunkt = vedtak.opprettetTidspunkt,
            innkrevingUtsattTilDato = vedtak.innkrevingUtsattTilDato,
            fastsattILand = vedtak.fastsattILand,
            grunnlagListe = grunnlagDtoListe,
            stønadsendringListe = stønadsendringListe.map { it.tilDto() },
            engangsbeløpListe = hentEngangsbeløpTilVedtak(engangsbeløpListe),
            behandlingsreferanseListe = behandlingsreferanseResponseListe,
        )
    }

    private fun Stønadsendring.tilDto(): StønadsendringDto {
        val grunnlagReferanseResponseListe = ArrayList<String>()
        val stønadsendringGrunnlagListe = persistenceService.hentAlleGrunnlagForStønadsendring(id)
        stønadsendringGrunnlagListe.forEach {
            val grunnlag = persistenceService.hentGrunnlag(it.grunnlag.id)
            grunnlagReferanseResponseListe.add(grunnlag.referanse)
        }
        val periodeListe = persistenceService.hentAllePerioderForStønadsendring(id)
        return StønadsendringDto(
            type = Stønadstype.valueOf(type),
            sak = Saksnummer(sak),
            skyldner = Personident(skyldner),
            kravhaver = Personident(kravhaver),
            mottaker = Personident(mottaker),
            førsteIndeksreguleringsår = førsteIndeksreguleringsår,
            innkreving = Innkrevingstype.valueOf(innkreving),
            beslutning = Beslutningstype.valueOf(beslutning),
            omgjørVedtakId = omgjørVedtakId,
            eksternReferanse = eksternReferanse,
            sisteVedtaksid = sisteVedtaksid?.toLong(),
            grunnlagReferanseListe = grunnlagReferanseResponseListe,
            periodeListe = hentPerioderTilVedtak(periodeListe),
        )
    }

    private fun hentPerioderTilVedtak(periodeListe: List<Periode>): List<VedtakPeriodeDto> {
        val periodeResponseListe = ArrayList<VedtakPeriodeDto>()
        periodeListe.forEach { periode ->
            val grunnlagReferanseResponseListe = ArrayList<String>()
            val periodeGrunnlagListe = persistenceService.hentAlleGrunnlagForPeriode(periode.id)
            periodeGrunnlagListe.forEach {
                val grunnlag = persistenceService.hentGrunnlag(it.grunnlag.id)
                grunnlagReferanseResponseListe.add(grunnlag.referanse)
            }
            periodeResponseListe.add(
                VedtakPeriodeDto(
                    periode = ÅrMånedsperiode(periode.fom, periode.til),
                    beløp = periode.beløp,
                    valutakode = periode.valutakode?.trimEnd(),
                    resultatkode = periode.resultatkode,
                    delytelseId = periode.delytelseId,
                    grunnlagReferanseListe = grunnlagReferanseResponseListe,
                ),
            )
        }
        return periodeResponseListe
    }

    private fun hentEngangsbeløpTilVedtak(engangsbeløpListe: List<Engangsbeløp>): List<EngangsbeløpDto> {
        val engangsbeløpResponseListe = ArrayList<EngangsbeløpDto>()
        engangsbeløpListe.forEach { dto ->
            val grunnlagReferanseResponseListe = ArrayList<String>()
            val engangsbeløpGrunnlagListe = persistenceService.hentAlleGrunnlagForEngangsbeløp(dto.id)
            engangsbeløpGrunnlagListe.forEach {
                val grunnlag = persistenceService.hentGrunnlag(it.grunnlag.id)
                grunnlagReferanseResponseListe.add(grunnlag.referanse)
            }
            engangsbeløpResponseListe.add(
                EngangsbeløpDto(
                    type = Engangsbeløptype.valueOf(dto.type),
                    sak = Saksnummer(dto.sak),
                    skyldner = Personident(dto.skyldner),
                    kravhaver = Personident(dto.kravhaver),
                    mottaker = Personident(dto.mottaker),
                    beløp = dto.beløp,
                    betaltBeløp = dto.betaltBeløp,
                    valutakode = dto.valutakode,
                    resultatkode = dto.resultatkode,
                    innkreving = Innkrevingstype.valueOf(dto.innkreving),
                    beslutning = Beslutningstype.valueOf(dto.beslutning),
                    omgjørVedtakId = dto.omgjørVedtakId,
                    referanse = dto.referanse,
                    delytelseId = dto.delytelseId,
                    eksternReferanse = dto.eksternReferanse,
                    grunnlagReferanseListe = grunnlagReferanseResponseListe,
                ),
            )
        }
        return engangsbeløpResponseListe
    }

    fun oppdaterVedtak(vedtaksid: Int, vedtakRequest: OpprettVedtakRequestDto): Int {
        if (vedtakRequest.grunnlagListe.isEmpty()) {
            val feilmelding = "Grunnlagsdata mangler fra OppdaterVedtakRequest"
            LOGGER.error(feilmelding)
            SECURE_LOGGER.error("$feilmelding: ${tilJson(vedtakRequest)}")
            throw GrunnlagsdataManglerException(feilmelding)
        }

        if (alleVedtaksdataMatcher(vedtaksid, vedtakRequest)) {
            slettEventueltEksisterendeGrunnlag(vedtaksid)
            oppdaterGrunnlag(vedtaksid, vedtakRequest)
        } else {
            val feilmelding = "Innsendte data for oppdatering av vedtak matcher ikke med eksisterende vedtaksdata"
            LOGGER.error(feilmelding)
            SECURE_LOGGER.error("$feilmelding: Request: $vedtakRequest \n\n Vedtak som oppdateres: ${hentVedtak(vedtaksid)} ")
            throw VedtaksdataMatcherIkkeException(feilmelding)
        }
        measureVedtak(oppdaterVedtakCounterName, vedtakRequest)

        return vedtaksid
    }

    // Hent alle vedtak for stønad
    fun hentVedtakForStønad(request: HentVedtakForStønadRequest): HentVedtakForStønadResponse {
        val skyldnerAllePersonidenter = identUtils.hentAlleIdenter(request.skyldner)
        val kravhaverAllePersonidenter = identUtils.hentAlleIdenter(request.kravhaver)
        val stønadsendringer = persistenceService.hentStønadsendringForStønad(request, skyldnerAllePersonidenter, kravhaverAllePersonidenter)
        return HentVedtakForStønadResponse(
            stønadsendringer
                .map { stønadsendring ->
                    val vedtak = stønadsendring.vedtak
                    VedtakForStønad(
                        vedtaksid = vedtak.id.toLong(),
                        vedtakstidspunkt = vedtak.vedtakstidspunkt!!,
                        type = Vedtakstype.valueOf(vedtak.type),
                        stønadsendring = stønadsendring.tilDto(),
                        behandlingsreferanser = persistenceService.hentAlleBehandlingsreferanserForVedtak(vedtak.id).map {
                            BehandlingsreferanseDto(BehandlingsrefKilde.valueOf(it.kilde), it.referanse)
                        },
                        kilde = Vedtakskilde.valueOf(vedtak.kilde),
                    )
                },
        )
    }

    fun oppdaterVedtaksforslag(vedtaksid: Int, vedtakRequest: OpprettVedtakRequestDto): Int {
        // sjekk om det finnes et vedtak for mottatt vedtaksid. Hvis det ikke finnes må det kastes en exception
        try {
            persistenceService.hentVedtak(vedtaksid)
        } catch (e: Exception) {
            val feilmelding = "Fant ikke vedtaksforslag med vedtaksid $vedtaksid"
            LOGGER.error(feilmelding)
            SECURE_LOGGER.error("$feilmelding: ${tilJson(vedtakRequest)}")
            throw IllegalArgumentException(feilmelding)
        }

        if (vedtakRequest.vedtakstidspunkt != null) {
            throw IllegalArgumentException(
                "Vedtakstidspunkt kan ikke være angitt ved oppdatering av vedtaksforslag. " +
                    "Bruk endepunkt for å fatte vedtak fra vedtaksforslag",
            )
        }

        val opprettetAv =
            vedtakRequest.opprettetAv.trimToNull()
                ?: TokenUtils.hentSaksbehandlerIdent()
                ?: TokenUtils.hentApplikasjonsnavn()
                ?: vedtakRequest.manglerOpprettetAv()
        val opprettetAvNavn = SaksbehandlernavnProvider.hentSaksbehandlernavn(opprettetAv)
        val kildeapplikasjon = TokenUtils.hentApplikasjonsnavn() ?: "UKJENT"

        val oppdatertVedtaksforslag = vedtakRequest.toVedtakEntity(
            opprettetAv = opprettetAv,
            opprettetAvNavn = opprettetAvNavn,
            kildeapplikasjon = kildeapplikasjon,
            vedtakstidspunkt = null,
        )

        // Setter vedtaksid lik mottatt vedtaksid for vedtaksforslaget og oppdaterer forekomsten i tabell vedtak
        oppdatertVedtaksforslag.id = vedtaksid
        persistenceService.oppdaterVedtaksforslag(oppdatertVedtaksforslag)

        // Alt annet lagret innhold på vedtaksforslaget slettes og opprettes på nytt basert på mottatt request for oppdatering
        slettEventueltEksisterendeGrunnlag(vedtaksid)
        slettStønadsendringerBehandlingsreferanserPerioderOgEngangsbeløpForVedtak(vedtaksid)

        val grunnlagIdRefMap = mutableMapOf<String, Int>()

        val engangsbeløpReferanseListe = mutableListOf<String>()

        // Grunnlag
        vedtakRequest.grunnlagListe.forEach {
            val opprettetGrunnlagId = opprettGrunnlag(it, oppdatertVedtaksforslag)
            grunnlagIdRefMap[it.referanse] = opprettetGrunnlagId.id
        }

        // Stønadsendring
        vedtakRequest.stønadsendringListe.forEach { opprettStønadsendring(it, oppdatertVedtaksforslag, grunnlagIdRefMap) }

        // Engangsbeløp
        vedtakRequest.engangsbeløpListe.forEach {
            engangsbeløpReferanseListe.add(opprettEngangsbeløp(it, oppdatertVedtaksforslag, grunnlagIdRefMap).referanse)
        }

        // Behandlingsreferanse
        vedtakRequest.behandlingsreferanseListe.forEach { opprettBehandlingsreferanse(it, oppdatertVedtaksforslag) }

        hendelserService.opprettHendelseVedtaksforslag(
            status = VedtaksforslagStatus.OPPDATERT,
            request = vedtakRequest,
            vedtakId = vedtaksid,
            saksnummer = vedtakRequest.stønadsendringListe.firstOrNull()?.sak,
        )

        return oppdatertVedtaksforslag.id
    }

    fun slettVedtaksforslag(vedtaksid: Int): Int {
        // sjekk om det finnes et vedtak for mottatt vedtaksid. Hvis det ikke finnes må det kastes en exception
        try {
            persistenceService.hentVedtak(vedtaksid)
        } catch (e: Exception) {
            val feilmelding = "Fant ikke vedtaksforslag med vedtaksid $vedtaksid"
            LOGGER.error(feilmelding)
            SECURE_LOGGER.error(feilmelding)
            throw IllegalArgumentException(feilmelding)
        }

        // sjekk at angitt vedtak  er et vedtaksforslag, skal ikke kunne slettes ellers
        val vedtak = persistenceService.hentVedtak(vedtaksid)
        if (vedtak.vedtakstidspunkt != null) {
            val feilmelding = "Vedtak er ikke vedtaksforslag og kan derfor ikke slettes: $vedtaksid"
            LOGGER.error(feilmelding)
            SECURE_LOGGER.error(feilmelding)
            throw IllegalArgumentException(feilmelding)
        }

        val saksnummer = persistenceService.hentAlleStønadsendringerForVedtak(vedtak.id).firstOrNull()?.sak

        slettEventueltEksisterendeGrunnlag(vedtaksid)
        slettStønadsendringerBehandlingsreferanserPerioderOgEngangsbeløpForVedtak(vedtaksid)
        persistenceService.slettVedtak(vedtaksid)

        hendelserService.opprettHendelseVedtaksforslag(
            status = VedtaksforslagStatus.SLETTET,
            request = null,
            vedtakId = vedtaksid,
            saksnummer = saksnummer?.let { Saksnummer(it) },
        )

        return vedtaksid
    }

    fun fattVedtakForVedtaksforslag(vedtaksid: Int): Int {
        // sjekk om det finnes et vedtak for mottatt vedtaksid. Hvis det ikke finnes må det kastes en exception
        try {
            persistenceService.hentVedtak(vedtaksid)
        } catch (e: Exception) {
            val feilmelding = "Fant ikke vedtaksforslag med vedtaksid $vedtaksid"
            LOGGER.error(feilmelding)
            SECURE_LOGGER.error(feilmelding)
            throw IllegalArgumentException(feilmelding)
        }

        // sjekk at angitt vedtak  er et vedtaksforslag, skal ikke kunne fattes vedtak ellers
        val vedtak = persistenceService.hentVedtak(vedtaksid)
        if (vedtak.vedtakstidspunkt != null) {
            val feilmelding = "Vedtak er allerede fattet. Ignorer forespørsel $vedtaksid"
            LOGGER.warn(feilmelding)
            SECURE_LOGGER.warn(feilmelding)
            return vedtaksid
        }

        val stønadsendringListe = persistenceService.hentAlleStønadsendringerForVedtak(vedtaksid)

        SECURE_LOGGER.info(tilJson(stønadsendringListe))

        stønadsendringListe.forEach { stønad ->
            if (!validerAtSisteVedtaksidErOk(
                    type = Stønadstype.valueOf(stønad.type),
                    saksnummer = Saksnummer(stønad.sak),
                    skyldner = Personident(stønad.skyldner),
                    kravhaver = Personident(stønad.kravhaver),
                    sisteVedtaksid = stønad.sisteVedtaksid?.toLong(),
                )
            ) {
                throw PreconditionFailedException("Angitt sisteVedtaksid er ikke lik lagret siste vedtaksid")
            }
        }

        // Alle kontroller er ok og vedtaket fattes
        vedtak.vedtakstidspunkt = LocalDateTime.now()
        persistenceService.oppdaterVedtak(vedtak)

        // Henter det opprettede vedtaket for å vurdere om det skal legges ut hendelse
        val vedtakDto = hentVedtak(vedtaksid)

        if ((vedtakDto.stønadsendringListe.isNotEmpty() || vedtakDto.engangsbeløpListe.isNotEmpty())) {
            hendelserService.opprettHendelseVedtak(
                vedtakDto = vedtakDto,
                vedtakId = vedtaksid,
            )
        }

        hendelserService.opprettHendelseVedtaksforslag(
            status = VedtaksforslagStatus.FATTET,
            request = null,
            vedtakId = vedtaksid,
            saksnummer = Saksnummer(stønadsendringListe.first().sak),
        )

        return vedtaksid
    }

    // Hent vedtaksdata
    fun hentVedtakForBehandlingsreferanse(kilde: BehandlingsrefKilde, behandlingsreferanse: String): List<Int> =
        persistenceService.hentVedtaksidForBehandlingsreferanse(kilde.name, behandlingsreferanse)

    private fun alleVedtaksdataMatcher(vedtaksid: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean = vedtakMatcher(vedtaksid, vedtakRequest) &&
        stønadsendringerOgPerioderMatcher(vedtaksid, vedtakRequest) &&
        engangsbeløpMatcher(vedtaksid, vedtakRequest) &&
        behandlingsreferanserMatcher(vedtaksid, vedtakRequest)

    private fun vedtakMatcher(vedtaksid: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean {
        val eksisterendeVedtak = persistenceService.hentVedtak(vedtaksid)
        return vedtakRequest.kilde.name == eksisterendeVedtak.kilde &&
            vedtakRequest.type.name == eksisterendeVedtak.type &&
            vedtakRequest.opprettetAv == eksisterendeVedtak.opprettetAv &&
            vedtakRequest.vedtakstidspunkt?.year == eksisterendeVedtak.vedtakstidspunkt?.year &&
            vedtakRequest.vedtakstidspunkt?.month == eksisterendeVedtak.vedtakstidspunkt?.month &&
            vedtakRequest.vedtakstidspunkt?.dayOfMonth == eksisterendeVedtak.vedtakstidspunkt?.dayOfMonth &&
            vedtakRequest.vedtakstidspunkt?.hour == eksisterendeVedtak.vedtakstidspunkt?.hour &&
            vedtakRequest.vedtakstidspunkt?.minute == eksisterendeVedtak.vedtakstidspunkt?.minute &&
            vedtakRequest.vedtakstidspunkt?.second == eksisterendeVedtak.vedtakstidspunkt?.second &&
            vedtakRequest.enhetsnummer?.verdi == eksisterendeVedtak.enhetsnummer &&
            vedtakRequest.innkrevingUtsattTilDato == eksisterendeVedtak.innkrevingUtsattTilDato &&
            vedtakRequest.fastsattILand == eksisterendeVedtak.fastsattILand
    }

    private fun stønadsendringerOgPerioderMatcher(vedtaksid: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean {
        // Sorterer begge listene likt
        val eksisterendeStønadsendringListe = persistenceService.hentAlleStønadsendringerForVedtak(vedtaksid)
            .sortedWith(compareBy({ it.type }, { it.skyldner }, { it.kravhaver }, { it.sak }))

        // vedtakRequest.stønadsendringListe kan være null, eksisterendeStønadsendringListe kan ikke være null,
        // bare emptyList
        if (vedtakRequest.stønadsendringListe.isEmpty()) {
            return eksisterendeStønadsendringListe.isEmpty()
        }

        // Sjekker om det er lagret like mange stønadsendringer som det ligger i oppdaterVedtak-requesten
        if (vedtakRequest.stønadsendringListe.size != eksisterendeStønadsendringListe.size) {
            SECURE_LOGGER.error(
                "Det er ulikt antall stønadsendringer i request for å oppdatere vedtak og det som er lagret på vedtaket fra før. VedtakId $vedtaksid",
            )
            return false
        }

        // Teller antall forekomster som matcher. Hvis antallet er lavere enn antall stønadsendringer
        // som ligger på vedtaket fra før så gjøres det et nytt forsøk på oppdaterte personidenter
        val antallMatchendeElementer = finnMatchendeStønadsendringer(eksisterendeStønadsendringListe, vedtakRequest.stønadsendringListe).size

        // Hvis det er mismatch så gjøres det en innhenting av nyeste personident for partene i stønadsendringen og deretter gjøres
        // et nytt forsøk på å matche
        if (antallMatchendeElementer != eksisterendeStønadsendringListe.size) {
            SECURE_LOGGER.warn(
                "Det er mismatch på minst én stønadsendring ved forsøk på å oppdatere vedtak, forsøker på nytt med oppdaterte personidenter. " +
                    "Vedtak: $vedtaksid: request: ${
                        tilJson(
                            vedtakRequest.stønadsendringListe,
                        )
                    } eksisterende: ${tilJson(eksisterendeStønadsendringListe)}",
            )

            // Kopierer requesten med oppdaterte identer
            val requestMedOppdaterteIdenter = vedtakRequest.copy(
                stønadsendringListe = vedtakRequest.stønadsendringListe.map { stønadsendring ->
                    val nyesteSkyldner = identUtils.hentNyesteIdent(stønadsendring.skyldner)
                    val nyesteKravhaver = identUtils.hentNyesteIdent(stønadsendring.kravhaver)

                    SECURE_LOGGER.info(
                        "Stønadsendring. Mottatt skyldner: ${stønadsendring.skyldner.verdi} kravhaver: ${stønadsendring.kravhaver.verdi} " +
                            "etter oppdatering, skyldner: ${nyesteSkyldner.verdi} kravhaver: ${nyesteKravhaver.verdi}",
                    )

                    stønadsendring.copy(
                        skyldner = nyesteSkyldner,
                        kravhaver = nyesteKravhaver,
                    )
                },
            )

            // Kopierer eksisterende stønadsendringer med oppdaterte identer
            val eksisterendeStønadsendringListeMedOppdaterteIdenter = eksisterendeStønadsendringListe.map { stønadsendring ->
                val nyesteSkyldner = identUtils.hentNyesteIdent(Personident(stønadsendring.skyldner)).verdi
                val nyesteKravhaver = identUtils.hentNyesteIdent(Personident(stønadsendring.kravhaver)).verdi

                SECURE_LOGGER.info(
                    "Stønadsendring. Eksisterende skyldner: ${stønadsendring.skyldner} kravhaver: ${stønadsendring.kravhaver} " +
                        "etter oppdatering, skyldner: $nyesteSkyldner kravhaver: $nyesteKravhaver",
                )
                stønadsendring.copy(
                    skyldner = nyesteSkyldner,
                    kravhaver = nyesteKravhaver,
                )
            }

            val antallMatchendeElementerOppdaterteIdenter =
                finnMatchendeStønadsendringer(
                    eksisterendeStønadsendringListeMedOppdaterteIdenter,
                    requestMedOppdaterteIdenter.stønadsendringListe,
                ).size

            if (antallMatchendeElementerOppdaterteIdenter != eksisterendeStønadsendringListeMedOppdaterteIdenter.size) {
                // Hvis det fortsatt er mismatch så kastes exception
                SECURE_LOGGER.error(
                    "Det er fortsatt mismatch etter å ha testet på nyeste personidenter på minst én stønadsendring ved forsøk på å oppdatere " +
                        "vedtak $vedtaksid: request: ${
                            tilJson(
                                requestMedOppdaterteIdenter.stønadsendringListe,
                            )
                        } eksisterende: ${tilJson(eksisterendeStønadsendringListeMedOppdaterteIdenter)}",
                )
                return false
            }
        }

        eksisterendeStønadsendringListe.forEach {
            stønadsendringsidGrunnlagSkalSlettesListe.add(it.id)
        }

        // Sorterer listene likt for å kunne sammenligne perioder
        val sortertStønadsendringRequestListe = vedtakRequest.stønadsendringListe
            .sortedWith(compareBy({ it.type.name }, { it.skyldner.verdi }, { it.kravhaver.verdi }, { it.sak.verdi }))

        for ((i, stønadsendring) in eksisterendeStønadsendringListe.withIndex()) {
            if (!perioderMatcher(stønadsendring.id, sortertStønadsendringRequestListe[i])) {
                SECURE_LOGGER.error(
                    "Det er mismatch på minst én periode ved forsøk på å oppdatere vedtak $vedtaksid, stønadsendring: ${stønadsendring.id}",
                )
                return false
            }
        }
        return true
    }

    private fun perioderMatcher(stønadsendringId: Int, stønadsendringRequest: OpprettStønadsendringRequestDto): Boolean {
        val eksisterendePeriodeListe = persistenceService.hentAllePerioderForStønadsendring(stønadsendringId)

        val matchendeElementer = stønadsendringRequest.periodeListe
            .filter { periodeRequest ->
                eksisterendePeriodeListe.any {
                    periodeRequest.periode.toDatoperiode().fom == it.fom &&
                        periodeRequest.periode.toDatoperiode().til == it.til &&
                        periodeRequest.beløp?.toInt() == it.beløp?.toInt() &&
                        periodeRequest.valutakode == it.valutakode &&
                        periodeRequest.resultatkode == it.resultatkode &&
                        periodeRequest.delytelseId == it.delytelseId
                }
            }

        if (matchendeElementer.size == eksisterendePeriodeListe.size) {
            eksisterendePeriodeListe.forEach {
                periodeidGrunnlagSkalSlettesListe.add(it.id)
            }
        }

        return matchendeElementer.size == eksisterendePeriodeListe.size
    }

    private fun engangsbeløpMatcher(vedtaksid: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean {
        val eksisterendeEngangsbeløpListe = persistenceService.hentAlleEngangsbeløpForVedtak(vedtaksid)

        // vedtakRequest.engangsbeløpListe kan være null, eksisterendeEngangsbeløpListe kan ikke være null,
        // bare emptyList
        if (vedtakRequest.engangsbeløpListe.isEmpty()) {
            return eksisterendeEngangsbeløpListe.isEmpty()
        }

        // Sjekker om det er lagret like mange engangsbeløp som det ligger i oppdaterVedtak-requesten
        if (vedtakRequest.engangsbeløpListe.size != eksisterendeEngangsbeløpListe.size) {
            SECURE_LOGGER.error(
                "Det er ulikt antall engangsbeløp i request for å oppdatere vedtak og det som er lagret på vedtaket fra før. VedtakId $vedtaksid: request: ${vedtakRequest.engangsbeløpListe}, eksisterende: $eksisterendeEngangsbeløpListe",
            )
            return false
        }

        // Teller antall forekomster som matcher. Hvis antallet er lavere enn antall engangsbeløp
        // som ligger på vedtaket fra før så feilmeldes det
        val antallMatchendeElementer = finnMatchendeEngangsbeløp(eksisterendeEngangsbeløpListe, vedtakRequest.engangsbeløpListe).size

        // Hvis det er mismatch så gjøres det en innhenting av nyeste personident for partene i engangsbeløpet og deretter gjøres
        // et nytt forsøk på å matche
        if (antallMatchendeElementer == eksisterendeEngangsbeløpListe.size) {
            eksisterendeEngangsbeløpListe.forEach {
                engangsbeløpsidGrunnlagSkalSlettesListe.add(it.id)
            }
            return true
        } else {
            SECURE_LOGGER.warn(
                "Det er mismatch på minst ett engangsbeløp ved forsøk på å oppdatere vedtak, forsøker på nytt med oppdaterte personidenter. " +
                    "Vedtak: $vedtaksid: request: ${
                        tilJson(
                            vedtakRequest.engangsbeløpListe,
                        )
                    } eksisterende: ${tilJson(eksisterendeEngangsbeløpListe)}",
            )

            // Kopierer requesten med oppdaterte identer
            val requestMedOppdaterteIdenter = vedtakRequest.copy(
                engangsbeløpListe = vedtakRequest.engangsbeløpListe.map { engangsbeløp ->
                    val nyesteSkyldner = identUtils.hentNyesteIdent(engangsbeløp.skyldner)
                    val nyesteKravhaver = identUtils.hentNyesteIdent(engangsbeløp.kravhaver)

                    SECURE_LOGGER.info(
                        "Engangsbeløp. Mottatt skyldner: ${engangsbeløp.skyldner.verdi} kravhaver: ${engangsbeløp.kravhaver.verdi} " +
                            "etter oppdatering, skyldner: ${nyesteSkyldner.verdi} kravhaver: ${nyesteKravhaver.verdi}",
                    )
                    engangsbeløp.copy(
                        skyldner = nyesteSkyldner,
                        kravhaver = nyesteKravhaver,
                    )
                },
            )

            // Kopierer eksisterende stønadsendringer med oppdaterte identer
            val eksisterendeEngangsbeløpListeMedOppdaterteIdenter = eksisterendeEngangsbeløpListe.map { engangsbeløp ->
                val skyldner = identUtils.hentNyesteIdent(Personident(engangsbeløp.skyldner)).verdi
                val kravhaver = identUtils.hentNyesteIdent(Personident(engangsbeløp.kravhaver)).verdi

                SECURE_LOGGER.info(
                    "Engangsbeløp. Eksisterende skyldner: ${engangsbeløp.skyldner} kravhaver: ${engangsbeløp.kravhaver} " +
                        "etter oppdatering, skyldner: $skyldner kravhaver: $kravhaver",
                )
                engangsbeløp.copy(
                    skyldner = skyldner,
                    kravhaver = kravhaver,
                )
            }

            val antallMatchendeElementerMedOppdaterteIdenter =
                finnMatchendeEngangsbeløp(
                    eksisterendeEngangsbeløpListeMedOppdaterteIdenter,
                    requestMedOppdaterteIdenter.engangsbeløpListe,
                ).size

            if (antallMatchendeElementerMedOppdaterteIdenter == eksisterendeEngangsbeløpListeMedOppdaterteIdenter.size) {
                eksisterendeEngangsbeløpListe.forEach {
                    engangsbeløpsidGrunnlagSkalSlettesListe.add(it.id)
                }
                return true
            } else {
                SECURE_LOGGER.error(
                    "Det er fortsatt mismatch på minst ett engangsbeløp med oppdaterte personidenter ved forsøk på å oppdatere vedtak . " +
                        "Vedtak: $vedtaksid: request: ${
                            tilJson(
                                requestMedOppdaterteIdenter.engangsbeløpListe,
                            )
                        } eksisterende: ${tilJson(eksisterendeEngangsbeløpListeMedOppdaterteIdenter)}",
                )
            }
            return false
        }
    }

    private fun behandlingsreferanserMatcher(vedtaksid: Int, vedtakRequest: OpprettVedtakRequestDto): Boolean {
        val eksisterendeBehandlingsreferanseListe = persistenceService.hentAlleBehandlingsreferanserForVedtak(vedtaksid)

        // vedtakRequest.engangsbeløpListe kan være null, eksisterendeEngangsbeløpListe kan ikke være null,
        // bare emptyList
        if (vedtakRequest.behandlingsreferanseListe.isEmpty()) {
            return eksisterendeBehandlingsreferanseListe.isEmpty()
        }

        // Sjekker om det er lagret like mange behandlinmgsreferanser som det ligger i oppdaterVedtak-requesten
        if (vedtakRequest.behandlingsreferanseListe.size != eksisterendeBehandlingsreferanseListe.size) {
            SECURE_LOGGER.error(
                "Det er ulikt antall behandlingsreferanser i request for å oppdatere vedtak og det som er lagret på vedtaket fra før. " +
                    "VedtakId: $vedtaksid",
            )
            return false
        }

        // Teller antall forekomster som matcher. Hvis antallet er lavere enn antall engangsbeløp
        // som ligger på vedtaket fra før så feilmeldes det
        val matchendeElementer = vedtakRequest.behandlingsreferanseListe
            .filter { behandlingsreferanseRequestListe ->
                eksisterendeBehandlingsreferanseListe.any {
                    behandlingsreferanseRequestListe.kilde.name == it.kilde &&
                        behandlingsreferanseRequestListe.referanse == it.referanse
                }
            }
        return matchendeElementer.size == eksisterendeBehandlingsreferanseListe.size
    }

    private fun slettEventueltEksisterendeGrunnlag(vedtaksid: Int) {
        val stønadsendringer = persistenceService.hentAlleStønadsendringerForVedtak(vedtaksid)

        stønadsendringer.forEach { stønadsendring ->
            persistenceService.slettAlleStønadsendringGrunnlagForStønadsendring(stønadsendring.id)
            val perioder = persistenceService.hentAllePerioderForStønadsendring(stønadsendring.id)
            perioder.forEach { periode ->
                persistenceService.slettAllePeriodeGrunnlagForPeriode(periode.id)
            }
        }

        // slett fra EngangsbeløpGrunnlag
        val engangsbeløpListe = persistenceService.hentAlleEngangsbeløpForVedtak(vedtaksid)
        engangsbeløpListe.forEach { engangsbeløp ->
            persistenceService.slettAlleEngangsbeløpGrunnlagForEngangsbeløp(engangsbeløp.id)
        }

        // slett fra Grunnlag
        persistenceService.slettAlleGrunnlagForVedtak(vedtaksid)
    }

    private fun slettStønadsendringerBehandlingsreferanserPerioderOgEngangsbeløpForVedtak(vedtaksid: Int) {
        persistenceService.slettAlleBehandlingsreferanserForVedtak(vedtaksid)
        persistenceService.slettAlleEngangsbeløpForVedtak(vedtaksid)

        val stønadsendringListe = persistenceService.stønadsendringRepository.hentAlleStønadsendringerForVedtak(vedtaksid)
        stønadsendringListe.forEach { stønadsendring ->
            persistenceService.slettAllePerioderForStønadsendring(stønadsendring.id)
            persistenceService.slettStønadsendring(stønadsendring.id)
        }
    }

    private fun oppdaterGrunnlag(vedtaksid: Int, vedtakRequest: OpprettVedtakRequestDto) {
        val vedtak = persistenceService.hentVedtak(vedtaksid)

        val grunnlagIdRefMap = mutableMapOf<String, Int>()

        // Lagrer grunnlag
        vedtakRequest.grunnlagListe.forEach {
            val opprettetGrunnlagId = opprettGrunnlag(it, vedtak)
            grunnlagIdRefMap[it.referanse] = opprettetGrunnlagId.id
        }

        // oppdaterer StønadsendringGrunnlag og PeriodeGrunnlag
        val eksisterendeStønadsendringListe = persistenceService.hentAlleStønadsendringerForVedtak(vedtaksid)

        vedtakRequest.stønadsendringListe.forEach { stønadsendringRequest ->
            // matcher mot eksisterende stønadsendringer for å finne stønadsendringsid for igjen å finne perioder som skal brukes
            // til å oppdatere PeriodeGrunnlag. Oppdaterer først StønadsendringGrunnlag.
            val stønadsendringsid = finnEksisterendeStønadsendringsid(stønadsendringRequest, eksisterendeStønadsendringListe)

            oppdaterStønadsendringGrunnlag(stønadsendringRequest, stønadsendringsid, grunnlagIdRefMap)

            val eksisterendePeriodeListe = persistenceService.hentAllePerioderForStønadsendring(stønadsendringsid)

            stønadsendringRequest.periodeListe.forEach { periode ->
                // matcher mot eksisterende perioder for å finne periodeId for å oppdatere PeriodeGrunnlag
                val periodeId = finnEksisterendePeriodeid(periode, eksisterendePeriodeListe)
                oppdaterPeriodeGrunnlag(periode, periodeId, grunnlagIdRefMap)
            }
        }

        // oppdaterer EngangsbeløpGrunnlag
        val eksisterendeEngangsbeløpListe = persistenceService.hentAlleEngangsbeløpForVedtak(vedtaksid)
        vedtakRequest.engangsbeløpListe.forEach { engangsbeløp ->
            // matcher mot eksisterende engangsbeløp for å finne engangsbeløpId for igjen å oppdatere EngangsbeløpGrunnlag
            val engangsbeløpId = finnTilhørendeEngangsbeløpId(engangsbeløp, eksisterendeEngangsbeløpListe)
            oppdaterEngangsbeløpGrunnlag(engangsbeløp, engangsbeløpId, grunnlagIdRefMap)
        }
    }

    private fun finnEksisterendeStønadsendringsid(
        stønadsendringRequest: OpprettStønadsendringRequestDto,
        eksisterendeStønadsendringListe: List<Stønadsendring>,
    ): Int {
        val matchendeEksisterendeStønadsendring = finnMatchendeStønadsendringer(eksisterendeStønadsendringListe, listOf(stønadsendringRequest))

        if (matchendeEksisterendeStønadsendring.size != 1) {
            SECURE_LOGGER.warn(
                "Feil ved forsøk på å hente stønadsendringsid under oppdatering av vedtak. Forsøker på nytt med oppdaterte personidenter: ${
                    tilJson(
                        stønadsendringRequest,
                    )
                }",
            )

            val nyesteSkyldner = identUtils.hentNyesteIdent(stønadsendringRequest.skyldner)
            val nyesteKravhaver = identUtils.hentNyesteIdent(stønadsendringRequest.kravhaver)

            SECURE_LOGGER.info(
                "Stønadsendring. Mottatt skyldner: ${stønadsendringRequest.skyldner.verdi} kravhaver: ${stønadsendringRequest.kravhaver.verdi} " +
                    "etter oppdatering, skyldner: ${nyesteSkyldner.verdi} kravhaver: ${nyesteKravhaver.verdi}",
            )

            // Kopierer requesten med oppdaterte identer
            val requestMedOppdaterteIdenter = stønadsendringRequest.copy(
                skyldner = nyesteSkyldner,
                kravhaver = nyesteKravhaver,
            )

            // Kopierer eksisterende stønadsendringer med oppdaterte identer
            val eksisterendeStønadsendringListeMedOppdaterteIdenter = eksisterendeStønadsendringListe.map { stønadsendring ->

                val nyesteSkyldner = identUtils.hentNyesteIdent(Personident(stønadsendring.skyldner)).verdi
                val nyesteKravhaver = identUtils.hentNyesteIdent(Personident(stønadsendring.kravhaver)).verdi

                SECURE_LOGGER.info(
                    "Stønadsendring. Eksisterende skyldner: ${stønadsendring.skyldner} kravhaver: ${stønadsendring.kravhaver} " +
                        "etter oppdatering, skyldner: $nyesteSkyldner kravhaver: $nyesteKravhaver",
                )
                Stønadsendring(
                    id = stønadsendring.id,
                    vedtak = stønadsendring.vedtak,
                    type = stønadsendring.type,
                    sak = stønadsendring.sak,
                    skyldner = identUtils.hentNyesteIdent(Personident(stønadsendring.skyldner)).verdi,
                    kravhaver = identUtils.hentNyesteIdent(Personident(stønadsendring.kravhaver)).verdi,
                    mottaker = stønadsendring.mottaker,
                    førsteIndeksreguleringsår = stønadsendring.førsteIndeksreguleringsår,
                    innkreving = stønadsendring.innkreving,
                    beslutning = stønadsendring.beslutning,
                    omgjørVedtakId = stønadsendring.omgjørVedtakId,
                    eksternReferanse = stønadsendring.eksternReferanse,
                )
            }

            val matchendeElementerOppdaterteIdenter =
                finnMatchendeStønadsendringer(
                    eksisterendeStønadsendringListeMedOppdaterteIdenter,
                    listOf(requestMedOppdaterteIdenter),
                )

            if (matchendeElementerOppdaterteIdenter.size != 1) {
                SECURE_LOGGER.error(
                    "Andre forsøk på å hente stønadsendringsid under oppdatering av vedtak feiler. Request: ${
                        tilJson(
                            requestMedOppdaterteIdenter,
                        )
                    }",
                )
                throw VedtaksdataMatcherIkkeException(
                    "Stønadsendringsid ikke funnet ved oppdatering av vedtak. Eksisterende stønadsendringer med oppdaterte identer: ${
                        tilJson(
                            eksisterendeStønadsendringListeMedOppdaterteIdenter,
                        )
                    }",
                )
            }
            return matchendeElementerOppdaterteIdenter.first().id
        }
        return matchendeEksisterendeStønadsendring.first().id
    }

    // Opprett StønadsendringGrunnlag
    private fun oppdaterStønadsendringGrunnlag(
        stønadsendringRequest: OpprettStønadsendringRequestDto,
        stønadsendringsid: Int,
        grunnlagIdRefMap: Map<String, Int>,
    ) {
        stønadsendringRequest.grunnlagReferanseListe.forEach {
            val grunnlagId = grunnlagIdRefMap.getOrDefault(it, 0)
            if (grunnlagId == 0) {
                val feilmelding = "grunnlagReferanse $it ikke funnet i intern mappingtabell"
                LOGGER.error(feilmelding)
                throw IllegalArgumentException(feilmelding)
            } else {
                val stønadsendringGrunnlagBo = StønadsendringGrunnlagBo(
                    stønadsendringsid = stønadsendringsid,
                    grunnlagsid = grunnlagId,
                )
                persistenceService.opprettStønadsendringGrunnlag(stønadsendringGrunnlagBo)
            }
        }
    }

    private fun finnEksisterendePeriodeid(periodeRequest: OpprettPeriodeRequestDto, eksisterendePeriodeListe: List<Periode>): Int {
        val matchendeEksisterendePeriode = eksisterendePeriodeListe
            .filter { eksisterendePeriode ->
                eksisterendePeriodeListe.any {
                    eksisterendePeriode.fom == periodeRequest.periode.toDatoperiode().fom &&
                        eksisterendePeriode.til == periodeRequest.periode.toDatoperiode().til &&
                        eksisterendePeriode.beløp?.toInt() == periodeRequest.beløp?.toInt() &&
                        eksisterendePeriode.valutakode == periodeRequest.valutakode &&
                        eksisterendePeriode.resultatkode == periodeRequest.resultatkode &&
                        eksisterendePeriode.delytelseId == periodeRequest.delytelseId
                }
            }

        if (matchendeEksisterendePeriode.size != 1) {
            SECURE_LOGGER.error("Det er mismatch på antall matchende perioder for stønadsendring: ${tilJson(periodeRequest)}")
            throw VedtaksdataMatcherIkkeException("Det er mismatch på antall matchende stønadsendringer: ${tilJson(periodeRequest)}")
        }
        return matchendeEksisterendePeriode.first().id
    }

    // Oppdater periode
    private fun oppdaterPeriodeGrunnlag(periodeRequest: OpprettPeriodeRequestDto, periodeId: Int, grunnlagIdRefMap: Map<String, Int>) {
        // PeriodeGrunnlag
        periodeRequest.grunnlagReferanseListe.forEach {
            val grunnlagId = grunnlagIdRefMap.getOrDefault(it, 0)
            if (grunnlagId == 0) {
                val feilmelding = "grunnlagReferanse $it ikke funnet i intern mappingtabell"
                LOGGER.error(feilmelding)
                throw IllegalArgumentException(feilmelding)
            } else {
                val periodeGrunnlagBo = PeriodeGrunnlagBo(
                    periodeid = periodeId,
                    grunnlagsid = grunnlagId,
                )
                persistenceService.opprettPeriodeGrunnlag(periodeGrunnlagBo)
            }
        }
    }

    // Finner generert db-id for eksisterende stønadsendring
    private fun finnTilhørendeEngangsbeløpId(
        engangsbeløpRequest: OpprettEngangsbeløpRequestDto,
        eksisterendeEngangsbeløpListe: List<Engangsbeløp>,
    ): Int {
        val matchendeEksisterendeEngangsbeløp = finnMatchendeEngangsbeløp(eksisterendeEngangsbeløpListe, listOf(engangsbeløpRequest))

        if (matchendeEksisterendeEngangsbeløp.isEmpty()) {
            SECURE_LOGGER.warn(
                "Feil ved forsøk på å hente engangsbeløpid under oppdatering av vedtak. Forsøker på nytt med oppdaterte personidenter: ${
                    tilJson(
                        engangsbeløpRequest,
                    )
                }",
            )

            val nyesteSkyldner = identUtils.hentNyesteIdent(engangsbeløpRequest.skyldner)
            val nyesteKravhaver = identUtils.hentNyesteIdent(engangsbeløpRequest.kravhaver)

            // Kopierer requesten med oppdaterte identer
            val requestMedOppdaterteIdenter = engangsbeløpRequest.copy(
                skyldner = nyesteSkyldner,
                kravhaver = nyesteKravhaver,
            )

            SECURE_LOGGER.info(
                "Engangsbeløp. Mottatt skyldner: ${engangsbeløpRequest.skyldner.verdi} kravhaver: ${engangsbeløpRequest.kravhaver.verdi} " +
                    "etter oppdatering, skyldner: ${nyesteSkyldner.verdi} kravhaver: ${nyesteKravhaver.verdi}",
            )

            // Kopierer eksisterende engangsbeløp med oppdaterte identer
            val eksisterendeEngangsbeløpListeMedOppdaterteIdenter = eksisterendeEngangsbeløpListe.map { engangsbeløp ->
                val nyesteSkyldner = identUtils.hentNyesteIdent(Personident(engangsbeløp.skyldner)).verdi
                val nyesteKravhaver = identUtils.hentNyesteIdent(Personident(engangsbeløp.kravhaver)).verdi

                SECURE_LOGGER.info(
                    "Engangsbeløp. Eksisterende skyldner: ${engangsbeløp.skyldner} kravhaver: ${engangsbeløp.kravhaver} " +
                        "etter oppdatering, skyldner: $nyesteSkyldner kravhaver: $nyesteKravhaver",
                )
                engangsbeløp.copy(
                    skyldner = nyesteSkyldner,
                    kravhaver = nyesteKravhaver,
                )
            }

            val matchendeEksisterendeEngangsbeløpMedOppdatertIdent =
                finnMatchendeEngangsbeløp(eksisterendeEngangsbeløpListeMedOppdaterteIdenter, listOf(requestMedOppdaterteIdenter))

            if (matchendeEksisterendeEngangsbeløpMedOppdatertIdent.size != 1) {
                SECURE_LOGGER.error(
                    "Det er fortsatt mismatch ved forsøk på å hente engangsbeløpsid. Request: ${tilJson(requestMedOppdaterteIdenter)}",
                )
                throw VedtaksdataMatcherIkkeException(
                    "Finner ikke engangsbeløpsid ved match av engangsbeløp. Eksisterende engangsbeløp: ${
                        tilJson(
                            eksisterendeEngangsbeløpListeMedOppdaterteIdenter,
                        )
                    }",
                )
            }
            return matchendeEksisterendeEngangsbeløpMedOppdatertIdent.first().id
        }
        return matchendeEksisterendeEngangsbeløp.first().id
    }

    // Opprett EngangsbeløpGrunnlag
    private fun oppdaterEngangsbeløpGrunnlag(
        engangsbeløpRequest: OpprettEngangsbeløpRequestDto,
        engangsbeløpId: Int,
        grunnlagIdRefMap: Map<String, Int>,
    ) {
        // EngangsbeløpGrunnlag
        engangsbeløpRequest.grunnlagReferanseListe.forEach {
            val grunnlagId = grunnlagIdRefMap.getOrDefault(it, 0)
            if (grunnlagId == 0) {
                val feilmelding = "grunnlagReferanse $it ikke funnet i intern mappingtabell"
                LOGGER.error(feilmelding)
                throw IllegalArgumentException(feilmelding)
            } else {
                val engangsbeløpGrunnlagBo = EngangsbeløpGrunnlagBo(
                    engangsbeløpsid = engangsbeløpId,
                    grunnlagsid = grunnlagId,
                )
                persistenceService.opprettEngangsbeløpGrunnlag(engangsbeløpGrunnlagBo)
            }
        }
    }

    // Hent vedtaksdata
    fun hentVedtakForUnikReferanse(unikReferanse: String): VedtakDto? {
        val vedtak = persistenceService.hentVedtakForUnikReferanse(unikReferanse) ?: return null
        val grunnlagDtoListe = ArrayList<GrunnlagDto>()
        val grunnlagListe = persistenceService.hentAlleGrunnlagForVedtak(vedtak.id)
        grunnlagListe.forEach {
            grunnlagDtoListe.add(it.toGrunnlagDto())
        }
        val stønadsendringListe = persistenceService.hentAlleStønadsendringerForVedtak(vedtak.id)
        val engangsbeløpListe = persistenceService.hentAlleEngangsbeløpForVedtak(vedtak.id)
        val behandlingsreferanseListe = persistenceService.hentAlleBehandlingsreferanserForVedtak(vedtak.id)
        val behandlingsreferanseResponseListe = ArrayList<BehandlingsreferanseDto>()
        behandlingsreferanseListe.forEach {
            behandlingsreferanseResponseListe.add(
                BehandlingsreferanseDto(BehandlingsrefKilde.valueOf(it.kilde), it.referanse),
            )
        }

        return VedtakDto(
            vedtaksid = vedtak.id.toLong(),
            kilde = Vedtakskilde.valueOf(vedtak.kilde),
            type = Vedtakstype.valueOf(vedtak.type),
            opprettetAv = vedtak.opprettetAv,
            opprettetAvNavn = vedtak.opprettetAvNavn,
            kildeapplikasjon = vedtak.kildeapplikasjon,
            vedtakstidspunkt = vedtak.vedtakstidspunkt,
            unikReferanse = vedtak.unikReferanse,
            enhetsnummer = if (vedtak.enhetsnummer != null) Enhetsnummer(vedtak.enhetsnummer) else null,
            opprettetTidspunkt = vedtak.opprettetTidspunkt,
            innkrevingUtsattTilDato = vedtak.innkrevingUtsattTilDato,
            fastsattILand = vedtak.fastsattILand,
            grunnlagListe = grunnlagDtoListe,
            stønadsendringListe = stønadsendringListe.map { it.tilDto() },
            engangsbeløpListe = hentEngangsbeløpTilVedtak(engangsbeløpListe),
            behandlingsreferanseListe = behandlingsreferanseResponseListe,
        )
    }

    fun measureVedtak(
        metrikkNavn: String,
        enhetsnummer: Enhetsnummer?,
        vedtakstype: Vedtakstype,
        stønadstype: Stønadstype?,
        engangsbeløptype: Engangsbeløptype?,
    ) {
        Counter.builder("opprett_vedtak").description("Teller antall vedtak som er opprettet med stønad- eller engangsbeløptype")
            .tag("enhet", enhetsnummer.toString()).tag("vedtak_type", vedtakstype.name)
            .tag("opprettet_av_app", TokenUtils.hentApplikasjonsnavn() ?: "UKJENT")
            .tag("stønadstype", stønadstype?.name ?: "NONE")
            .tag("engangsbeløp_type", engangsbeløptype?.name ?: "NONE")
            .register(meterRegistry).increment()
    }

    fun measureVedtak(navn: String, vedtakRequest: OpprettVedtakRequestDto) {
        try {
            val enhetsnummer = vedtakRequest.enhetsnummer
            val vedtakstype = vedtakRequest.type
            vedtakRequest.stønadsendringListe.forEach {
                measureVedtak(navn, enhetsnummer, vedtakstype, it.type, null)
            }
            vedtakRequest.engangsbeløpListe.forEach {
                measureVedtak(navn, enhetsnummer, vedtakstype, null, it.type)
            }
        } catch (e: Exception) {
            LOGGER.error("Det skjedde en feil ved telling av metrikker", e)
        }
    }

    fun duplikateReferanser(engangsbeløpListe: List<OpprettEngangsbeløpRequestDto>): Boolean = engangsbeløpListe.groupBy {
        it.referanse
    }.any { it.value.size > 1 }

    private fun genererUnikReferanse(vedtaksid: Int): String {
        var referanse = UUID.randomUUID().toString()
        while (!persistenceService.referanseErUnik(vedtaksid, referanse)) {
            referanse = genererUnikReferanse(vedtaksid)
        }
        return referanse
    }

    private fun validerAtSisteVedtaksidErOk(
        type: Stønadstype,
        saksnummer: Saksnummer,
        skyldner: Personident,
        kravhaver: Personident,
        sisteVedtaksid: Long?,
    ): Boolean {
        val skyldnerAllePersonidenter = identUtils.hentAlleIdenter(skyldner)
        val kravhaverAllePersonidenter = identUtils.hentAlleIdenter(kravhaver)
        val sisteVedtaksidForStønad = persistenceService.hentSisteVedtaksidForStønad(
            saksnummer.verdi,
            type.name,
            skyldnerAllePersonidenter,
            kravhaverAllePersonidenter,
        ).toLong()
        if (sisteVedtaksid != sisteVedtaksidForStønad) {
            LOGGER.error("Angitt sisteVedtaksid: $sisteVedtaksid for sak: $saksnummer er ikke lik lagret siste vedtaksid: $sisteVedtaksidForStønad")
            val feilmelding =
                "Angitt sisteVedtaksid: $sisteVedtaksid for stønad $saksnummer $type $skyldner $kravhaver: " +
                    "er ikke lik lagret siste vedtaksid: $sisteVedtaksidForStønad"
            SECURE_LOGGER.error(feilmelding)
            return false
        }

        return true
    }

    private fun finnMatchendeStønadsendringer(
        eksisterendeStønadsendringListe: List<Stønadsendring>,
        requestStønadsendringListe: List<OpprettStønadsendringRequestDto>,
    ) = eksisterendeStønadsendringListe
        .filter { eksisterendeStønadsendring ->
            requestStønadsendringListe.any {
                eksisterendeStønadsendring.type == it.type.name &&
                    eksisterendeStønadsendring.sak == it.sak.verdi &&
                    eksisterendeStønadsendring.skyldner == it.skyldner.verdi &&
                    eksisterendeStønadsendring.kravhaver == it.kravhaver.verdi &&
                    eksisterendeStønadsendring.førsteIndeksreguleringsår == it.førsteIndeksreguleringsår &&
                    eksisterendeStønadsendring.innkreving == it.innkreving.name &&
                    eksisterendeStønadsendring.beslutning == it.beslutning.name &&
                    eksisterendeStønadsendring.omgjørVedtakId == it.omgjørVedtakId &&
                    eksisterendeStønadsendring.eksternReferanse == it.eksternReferanse
            }
        }

    private fun finnMatchendeEngangsbeløp(
        eksisterendeEngangsbeløpListe: List<Engangsbeløp>,
        requestEngangsbeløpListe: List<OpprettEngangsbeløpRequestDto>,
    ) = eksisterendeEngangsbeløpListe
        .filter { eksisterendeEngangsbeløp ->
            requestEngangsbeløpListe.any {
                eksisterendeEngangsbeløp.type == it.type.name &&
                    eksisterendeEngangsbeløp.sak == it.sak.verdi &&
                    eksisterendeEngangsbeløp.skyldner == it.skyldner.verdi &&
                    eksisterendeEngangsbeløp.kravhaver == it.kravhaver.verdi &&
                    eksisterendeEngangsbeløp.innkreving == it.innkreving.name &&
                    eksisterendeEngangsbeløp.beslutning == it.beslutning.name &&
                    eksisterendeEngangsbeløp.omgjørVedtakId == it.omgjørVedtakId &&
                    eksisterendeEngangsbeløp.referanse == it.referanse &&
                    eksisterendeEngangsbeløp.eksternReferanse == it.eksternReferanse
            }
        }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(VedtakService::class.java)
    }
}
