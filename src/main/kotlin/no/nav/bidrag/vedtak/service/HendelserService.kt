package no.nav.bidrag.vedtak.service

import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.transport.behandling.vedtak.Behandlingsreferanse
import no.nav.bidrag.transport.behandling.vedtak.Engangsbeløp
import no.nav.bidrag.transport.behandling.vedtak.Periode
import no.nav.bidrag.transport.behandling.vedtak.Sporingsdata
import no.nav.bidrag.transport.behandling.vedtak.Stønadsendring
import no.nav.bidrag.transport.behandling.vedtak.VedtakHendelse
import no.nav.bidrag.transport.behandling.vedtak.VedtaksforslagHendelse
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtakRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtaksforslagRequestDto
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
    ) {
        val vedtakHendelse = VedtakHendelse(
            kilde = vedtakRequest.kilde,
            type = vedtakRequest.type,
            id = vedtakId,
            vedtakstidspunkt = vedtakRequest.vedtakstidspunkt,
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
        vedtakKafkaEventProducer.publish(vedtakHendelse)
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

    fun opprettHendelseVedtaksforslag(
        request: OpprettVedtaksforslagRequestDto,
        vedtakId: Int,
        opprettetTidspunkt: LocalDateTime,
        opprettetAv: String,
        opprettetAvNavn: String?,
        kildeapplikasjon: String,
    ) {
        val vedtaksforslagHendelse = VedtaksforslagHendelse(
            status = ,
            kilde = request.kilde,
            type = request.type,
            id = vedtakId,
            vedtakstidspunkt = request.vedtakstidspunkt,
            enhetsnummer = request.enhetsnummer,
            opprettetAv = opprettetAv,
            opprettetAvNavn = opprettetAvNavn,
            kildeapplikasjon = kildeapplikasjon,
            opprettetTidspunkt = opprettetTidspunkt,
            innkrevingUtsattTilDato = request.innkrevingUtsattTilDato,
            fastsattILand = request.fastsattILand,
            stønadsendringListe = mapStønadsendringer(request),
            engangsbeløpListe = mapEngangsbeløp(request),
            behandlingsreferanseListe = mapBehandlingsreferanser(request),
            sporingsdata = Sporingsdata(CorrelationId.fetchCorrelationIdForThread()),
        )
        vedtakKafkaEventProducer.publish(vedtaksforslagHendelse)
        SECURE_LOGGER.info("Ny melding lagt på topic vedtak: ${tilJson(vedtaksforslagHendelse)}")
    }
}
