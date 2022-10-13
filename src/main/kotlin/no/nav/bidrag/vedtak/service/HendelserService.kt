package no.nav.bidrag.vedtak.service

import no.nav.bidrag.behandling.felles.dto.vedtak.*
import no.nav.bidrag.vedtak.SECURE_LOGGER
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class HendelserService(private val vedtakKafkaEventProducer: VedtakKafkaEventProducer) {

  fun opprettHendelse(
    vedtakRequest: OpprettVedtakRequestDto,
    vedtakId: Int,
    opprettetTimestamp: LocalDateTime
  ) {
    val vedtakHendelse = VedtakHendelse(
      vedtakType = vedtakRequest.vedtakType,
      vedtakId = vedtakId,
      vedtakDato = vedtakRequest.vedtakDato,
      enhetId = vedtakRequest.enhetId,
      opprettetAv = vedtakRequest.opprettetAv,
      opprettetTidspunkt = opprettetTimestamp,
      stonadsendringListe = mapStonadsendringer(vedtakRequest),
      engangsbelopListe = mapEngangsbelop(vedtakRequest)
    )
    vedtakKafkaEventProducer.publish(vedtakHendelse)
    SECURE_LOGGER.info("ny melding lagt på topic vedtak: $vedtakHendelse")
  }

  private fun mapStonadsendringer(vedtakRequest: OpprettVedtakRequestDto): List<Stonadsendring> {
    val stonadsendringListe = mutableListOf<Stonadsendring>()
    vedtakRequest.stonadsendringListe?.forEach {
      val periodeListe = mutableListOf<Periode>()
      it.periodeListe.forEach { periode ->
        periodeListe.add(
          Periode(
            periodeFomDato = periode.periodeFomDato,
            periodeTilDato = periode.periodeTilDato,
            belop = periode.belop,
            valutakode = periode.valutakode,
            resultatkode = periode.resultatkode
          )
        )
      }

      stonadsendringListe.add(
        Stonadsendring(
          stonadType = it.stonadType,
          sakId = it.sakId,
          skyldnerId = it.skyldnerId,
          kravhaverId = it.kravhaverId,
          mottakerId = it.mottakerId,
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
          endrerEngangsbelopId = it.endrerEngangsbelopId,
          type = it.type,
          sakId = it.sakId,
          skyldnerId = it.skyldnerId,
          kravhaverId = it.kravhaverId,
          mottakerId = it.mottakerId,
          belop = it.belop,
          valutakode = it.valutakode,
          resultatkode = it.resultatkode
        )
      )
    }
    return engangsbelopListe
  }
}