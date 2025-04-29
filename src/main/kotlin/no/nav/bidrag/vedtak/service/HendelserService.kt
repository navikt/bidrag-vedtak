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
import no.nav.bidrag.vedtak.SECURE_LOGGER
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import no.nav.bidrag.vedtak.util.VedtakUtil.Companion.tilJson
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class HendelserService(private val vedtakKafkaEventProducer: VedtakKafkaEventProducer) {

    fun opprettHendelseVedtak(
        vedtakRequest: OpprettVedtakRequestDto,
        vedtakId: Int,
        opprettetTidspunkt: LocalDateTime,
        opprettetAv: String,
        opprettetAvNavn: String?,
        kildeapplikasjon: String,
        vedtakstidspunkt: LocalDateTime,
    ) {
        val vedtakHendelse = VedtakHendelse(
            kilde = vedtakRequest.kilde,
            type = vedtakRequest.type,
            id = vedtakId,
            vedtakstidspunkt = vedtakstidspunkt,
            enhetsnummer = vedtakRequest.enhetsnummer,
            opprettetAv = opprettetAv,
            opprettetAvNavn = opprettetAvNavn,
            kildeapplikasjon = kildeapplikasjon,
            opprettetTidspunkt = opprettetTidspunkt,
            innkrevingUtsattTilDato = vedtakRequest.innkrevingUtsattTilDato,
            fastsattILand = vedtakRequest.fastsattILand,
            stønadsendringListe = mapStønadsendringer(vedtakRequest),
            engangsbeløpListe = mapEngangsbeløp(vedtakRequest),
            behandlingsreferanseListe = mapBehandlingsreferanser(vedtakRequest),
            sporingsdata = Sporingsdata(CorrelationId.fetchCorrelationIdForThread()),
        )
        vedtakKafkaEventProducer.publishVedtak(vedtakHendelse)
        SECURE_LOGGER.info("Ny melding lagt på topic vedtak: ${tilJson(vedtakHendelse)}")
    }

    private fun mapStønadsendringer(vedtakRequest: OpprettVedtakRequestDto): List<Stønadsendring> {
        val stønadsendringListe = mutableListOf<Stønadsendring>()
        vedtakRequest.stønadsendringListe.forEach {
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

    private fun mapEngangsbeløp(vedtakRequest: OpprettVedtakRequestDto): List<Engangsbeløp> {
        val engangsbeløpListe = mutableListOf<Engangsbeløp>()
        vedtakRequest.engangsbeløpListe.forEach {
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
                    referanse = it.referanse ?: "",
                    delytelseId = it.delytelseId,
                    eksternReferanse = it.eksternReferanse,
                ),
            )
        }
        return engangsbeløpListe
    }

    private fun mapBehandlingsreferanser(vedtakRequest: OpprettVedtakRequestDto): List<Behandlingsreferanse> {
        val behandlingsreferanseListe = mutableListOf<Behandlingsreferanse>()
        vedtakRequest.behandlingsreferanseListe.forEach {
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
