package no.nav.bidrag.vedtak.service

import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.domene.enums.vedtak.VedtaksforslagStatus
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.transport.behandling.vedtak.Behandlingsreferanse
import no.nav.bidrag.transport.behandling.vedtak.Engangsbeløp
import no.nav.bidrag.transport.behandling.vedtak.Periode
import no.nav.bidrag.transport.behandling.vedtak.Sporingsdata
import no.nav.bidrag.transport.behandling.vedtak.Stønadsendring
import no.nav.bidrag.transport.behandling.vedtak.VedtakHendelse
import no.nav.bidrag.transport.behandling.vedtak.VedtaksforslagHendelse
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtakRequestDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.vedtak.SECURE_LOGGER
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import no.nav.bidrag.vedtak.util.VedtakUtil.Companion.tilJson
import org.springframework.stereotype.Service

@Service
class HendelserService(private val vedtakKafkaEventProducer: VedtakKafkaEventProducer) {

    fun opprettHendelseVedtak(vedtakDto: VedtakDto, vedtakId: Int) {
        val vedtakHendelse = VedtakHendelse(
            kilde = vedtakDto.kilde,
            type = vedtakDto.type,
            id = vedtakId,
            vedtakstidspunkt = vedtakDto.vedtakstidspunkt!!,
            enhetsnummer = vedtakDto.enhetsnummer,
            opprettetAv = vedtakDto.opprettetAv,
            opprettetAvNavn = vedtakDto.opprettetAvNavn,
            kildeapplikasjon = vedtakDto.kildeapplikasjon,
            opprettetTidspunkt = vedtakDto.opprettetTidspunkt,
            innkrevingUtsattTilDato = vedtakDto.innkrevingUtsattTilDato,
            fastsattILand = vedtakDto.fastsattILand,
            stønadsendringListe = mapStønadsendringer(vedtakDto),
            engangsbeløpListe = mapEngangsbeløp(vedtakDto),
            behandlingsreferanseListe = mapBehandlingsreferanser(vedtakDto),
            sporingsdata = Sporingsdata(CorrelationId.fetchCorrelationIdForThread()),
        )
        vedtakKafkaEventProducer.publishVedtak(vedtakHendelse)
        SECURE_LOGGER.info("Ny melding lagt på topic vedtak: ${tilJson(vedtakHendelse)}")
    }

    private fun mapStønadsendringer(vedtakDto: VedtakDto): List<Stønadsendring> {
        val stønadsendringListe = mutableListOf<Stønadsendring>()
        vedtakDto.stønadsendringListe.forEach {
            val periodeListe = mutableListOf<Periode>()
            it.periodeListe.forEach { periode ->
                periodeListe.add(
                    Periode(
                        periode = periode.periode,
                        beløp = periode.beløp,
                        valutakode = periode.valutakode,
                        resultatkode = periode.resultatkode,
                        delytelseId = periode.delytelseId,
                    ),
                )
            }

            stønadsendringListe.add(
                Stønadsendring(
                    type = it.type,
                    sak = it.sak,
                    skyldner = it.skyldner,
                    kravhaver = it.kravhaver,
                    mottaker = it.mottaker,
                    førsteIndeksreguleringsår = it.førsteIndeksreguleringsår,
                    innkreving = it.innkreving,
                    beslutning = it.beslutning,
                    omgjørVedtakId = it.omgjørVedtakId,
                    eksternReferanse = it.eksternReferanse,
                    periodeListe = periodeListe,
                ),
            )
        }
        return stønadsendringListe
    }

    private fun mapEngangsbeløp(vedtakDto: VedtakDto): List<Engangsbeløp> {
        val engangsbeløpListe = mutableListOf<Engangsbeløp>()
        vedtakDto.engangsbeløpListe.forEach {
            engangsbeløpListe.add(
                Engangsbeløp(
                    type = it.type,
                    sak = it.sak,
                    skyldner = it.skyldner,
                    kravhaver = it.kravhaver,
                    mottaker = it.mottaker,
                    beløp = it.beløp,
                    betaltBeløp = it.betaltBeløp,
                    valutakode = it.valutakode,
                    resultatkode = it.resultatkode,
                    innkreving = it.innkreving,
                    beslutning = it.beslutning,
                    omgjørVedtakId = it.omgjørVedtakId,
                    referanse = it.referanse,
                    delytelseId = it.delytelseId,
                    eksternReferanse = it.eksternReferanse,
                ),
            )
        }
        return engangsbeløpListe
    }

    private fun mapBehandlingsreferanser(vedtakDto: VedtakDto): List<Behandlingsreferanse> {
        val behandlingsreferanseListe = mutableListOf<Behandlingsreferanse>()
        vedtakDto.behandlingsreferanseListe.forEach {
            behandlingsreferanseListe.add(
                Behandlingsreferanse(
                    kilde = it.kilde.toString(),
                    referanse = it.referanse,
                ),
            )
        }
        return behandlingsreferanseListe
    }

    fun opprettHendelseVedtaksforslag(status: VedtaksforslagStatus, request: OpprettVedtakRequestDto?, vedtakId: Int, saksnummer: Saksnummer?) {
        // Lager liste med unike saksnummer
        val saksnummerListe = request?.stønadsendringListe
            ?.map { it.sak.verdi }
            ?.toSet()
            ?.map { Saksnummer(it) }
        val vedtaksforslagHendelse = VedtaksforslagHendelse(
            status = status,
            vedtaksid = vedtakId,
            saksnummerListe = saksnummerListe ?: saksnummer?.let { listOf(it) } ?: emptyList(),
            sporingsdata = Sporingsdata(CorrelationId.fetchCorrelationIdForThread()),
        )
        vedtakKafkaEventProducer.publishVedtaksforslag(vedtaksforslagHendelse)
        SECURE_LOGGER.info("Ny melding lagt på topic vedtaksforslag: ${tilJson(vedtaksforslagHendelse)}")
    }
}
