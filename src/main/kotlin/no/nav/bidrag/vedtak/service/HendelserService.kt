package no.nav.bidrag.vedtak.service

import no.nav.bidrag.behandling.felles.dto.vedtak.Engangsbelop
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.Periode
import no.nav.bidrag.behandling.felles.dto.vedtak.Sporingsdata
import no.nav.bidrag.behandling.felles.dto.vedtak.Stonadsendring
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakHendelse
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.vedtak.SECURE_LOGGER
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class HendelserService(private val vedtakKafkaEventProducer: VedtakKafkaEventProducer) {

    fun opprettHendelse(
        vedtakRequest: OpprettVedtakRequestDto,
        vedtakId: Int,
        opprettetTidspunkt: LocalDateTime
    ) {
        val vedtakHendelse = VedtakHendelse(
            kilde = vedtakRequest.kilde,
            type = vedtakRequest.type,
            id = vedtakId,
            vedtakTidspunkt = vedtakRequest.vedtakTidspunkt,
            enhetId = vedtakRequest.enhetId,
            opprettetAv = vedtakRequest.opprettetAv,
            opprettetAvNavn = vedtakRequest.opprettetAvNavn,
            opprettetTidspunkt = opprettetTidspunkt,
            utsattTilDato = vedtakRequest.utsattTilDato,
            stonadsendringListe = mapStonadsendringer(vedtakRequest),
            engangsbelopListe = mapEngangsbelop(vedtakRequest),
            sporingsdata = Sporingsdata(
                CorrelationId.fetchCorrelationIdForThread()
                    ?: CorrelationId.generateTimestamped(vedtakRequest.type.toString())
                        .get()
            )
        )
        vedtakKafkaEventProducer.publish(vedtakHendelse)
        SECURE_LOGGER.info("Ny melding lagt på topic vedtak: $vedtakHendelse")
    }

    private fun mapStonadsendringer(vedtakRequest: OpprettVedtakRequestDto): List<Stonadsendring> {
        val stonadsendringListe = mutableListOf<Stonadsendring>()
        vedtakRequest.stonadsendringListe?.forEach {
            val periodeListe = mutableListOf<Periode>()
            it.periodeListe.forEach { periode ->
                periodeListe.add(
                    Periode(
                        fomDato = periode.fomDato,
                        tilDato = periode.tilDato,
                        belop = periode.belop,
                        valutakode = periode.valutakode,
                        resultatkode = periode.resultatkode,
                        delytelseId = periode.delytelseId
                    )
                )
            }

            stonadsendringListe.add(
                Stonadsendring(
                    type = it.type,
                    sakId = it.sakId,
                    skyldnerId = it.skyldnerId,
                    kravhaverId = it.kravhaverId,
                    mottakerId = it.mottakerId,
                    indeksreguleringAar = it.indeksreguleringAar,
                    innkreving = it.innkreving,
                    endring = it.endring,
                    omgjorVedtakId = it.omgjorVedtakId,
                    eksternReferanse = it.eksternReferanse,
                    periodeListe = periodeListe
                )
            )
        }
        return stonadsendringListe
    }

    private fun mapEngangsbelop(vedtakRequest: OpprettVedtakRequestDto): List<Engangsbelop> {
        val engangsbelopListe = mutableListOf<Engangsbelop>()
        vedtakRequest.engangsbelopListe?.forEach {
            engangsbelopListe.add(
                Engangsbelop(
                    type = it.type,
                    sakId = it.sakId,
                    skyldnerId = it.skyldnerId,
                    kravhaverId = it.kravhaverId,
                    mottakerId = it.mottakerId,
                    belop = it.belop,
                    valutakode = it.valutakode,
                    resultatkode = it.resultatkode,
                    innkreving = it.innkreving,
                    endring = it.endring,
                    omgjorVedtakId = it.omgjorVedtakId,
                    referanse = it.referanse,
                    delytelseId = it.delytelseId,
                    eksternReferanse = it.eksternReferanse
                )
            )
        }
        return engangsbelopListe
    }
}
