package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.api.vedtak.OpprettKomplettVedtakRequest
import no.nav.bidrag.vedtak.model.VedtakHendelsePeriode
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import no.nav.bidrag.vedtak.model.VedtakHendelse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class HendelserService(private val vedtakKafkaEventProducer: VedtakKafkaEventProducer) {

  companion object {
    private val LOGGER = LoggerFactory.getLogger(HendelserService::class.java)
  }

  fun opprettHendelse(request: OpprettKomplettVedtakRequest, vedtakId: Int, opprettetTimestamp: LocalDateTime) {
    if (request.stonadsendringListe.isNotEmpty()) {
      val vedtakHendelser = mapVedtakshendelser(request, vedtakId, opprettetTimestamp)
      vedtakHendelser.forEach { vedtakHendelse -> vedtakKafkaEventProducer.publish(vedtakHendelse) }
    }
  }

  private fun mapVedtakshendelser(
    request: OpprettKomplettVedtakRequest, vedtakId: Int, opprettetTimestamp: LocalDateTime):List<VedtakHendelse> {
    val vedtakshendelser = mutableListOf<VedtakHendelse>()
    request.stonadsendringListe.forEach {
      val vedtakHendelsePeriodeListe = mutableListOf<VedtakHendelsePeriode>()
      val vedtakId = it.vedtakId
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
          stonadType = it.stonadType,
          sakId = it.sakId,
          skyldnerId = it.skyldnerId,
          kravhaverId = it.kravhaverId,
          mottakerId = it.mottakerId,
          opprettetAvSaksbehandlerId = request.saksbehandlerId,
          opprettetTimestamp = opprettetTimestamp,
          periodeListe = vedtakHendelsePeriodeListe
        )
      )
    }

    return  vedtakshendelser
  }
}