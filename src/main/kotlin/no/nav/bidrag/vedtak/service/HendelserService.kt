package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.vedtak.OpprettVedtakRequest
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import no.nav.bidrag.vedtak.model.VedtakHendelse
import no.nav.bidrag.vedtak.model.VedtakHendelsePeriode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class HendelserService(private val vedtakKafkaEventProducer: VedtakKafkaEventProducer) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(HendelserService::class.java)
  }

  fun opprettHendelse(vedtakRequest: OpprettVedtakRequest, vedtakId: Int, opprettetTimestamp: LocalDateTime) {
      val vedtakHendelser = mapVedtakshendelser(vedtakRequest, vedtakId, opprettetTimestamp)
      vedtakHendelser.forEach { vedtakHendelse -> vedtakKafkaEventProducer.publish(vedtakHendelse) }
  }

  private fun mapVedtakshendelser(
    vedtakRequest: OpprettVedtakRequest, vedtakId: Int, opprettetTimestamp: LocalDateTime):List<VedtakHendelse> {
    val vedtakshendelser = mutableListOf<VedtakHendelse>()
    vedtakRequest.stonadsendringListe?.forEach {
      val vedtakHendelsePeriodeListe = mutableListOf<VedtakHendelsePeriode>()
      it.periodeListe.forEach { periode ->
        vedtakHendelsePeriodeListe.add(
          VedtakHendelsePeriode(
            periodeFom = periode.periodeFomDato,
            periodeTil = periode.periodeTilDato,
            belop = periode.belop,
            valutakode = periode.valutakode,
            resultatkode = periode.resultatkode
          )
        )
      }

      vedtakshendelser.add(
        VedtakHendelse(
          vedtakId = vedtakId,
          vedtakType = vedtakRequest.vedtakType,
          stonadType = it.stonadType,
          sakId = it.sakId,
          skyldnerId = it.skyldnerId,
          kravhaverId = it.kravhaverId,
          mottakerId = it.mottakerId,
          opprettetAv = vedtakRequest.opprettetAv,
          opprettetTimestamp = opprettetTimestamp,
          periodeListe = vedtakHendelsePeriodeListe
        )
      )
    }

    return  vedtakshendelser
  }
}