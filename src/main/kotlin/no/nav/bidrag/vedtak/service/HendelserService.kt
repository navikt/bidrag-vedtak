package no.nav.bidrag.vedtak.service

import no.nav.bidrag.behandling.felles.dto.vedtak.Engangsbelop
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.Periode
import no.nav.bidrag.behandling.felles.dto.vedtak.Stonadsendring
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakHendelse
import no.nav.bidrag.vedtak.SECURE_LOGGER
import no.nav.bidrag.vedtak.bo.EngangsbelopBo
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class HendelserService(private val vedtakKafkaEventProducer: VedtakKafkaEventProducer) {

  fun opprettHendelse(
    vedtakRequest: OpprettVedtakRequestDto,
    engangsbelopBoListe: ArrayList<EngangsbelopBo>?,
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
      eksternReferanse = vedtakRequest.eksternReferanse,
      utsattTilDato = vedtakRequest.utsattTilDato,
      stonadsendringListe = mapStonadsendringer(vedtakRequest),
      engangsbelopListe = mapEngangsbelop(engangsbelopBoListe)
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
            periodeFomDato = periode.periodeFomDato,
            periodeTilDato = periode.periodeTilDato,
            belop = periode.belop,
            valutakode = periode.valutakode,
            resultatkode = periode.resultatkode,
            referanse = periode.referanse
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
          indeksreguleringAar = it.indeksreguleringAar,
          periodeListe = periodeListe
        )
      )
    }
    return stonadsendringListe
  }

  private fun mapEngangsbelop(engangsbelopBoListe: ArrayList<EngangsbelopBo>?): List<Engangsbelop> {
    val engangsbelopListe = mutableListOf<Engangsbelop>()
    engangsbelopBoListe?.forEach {
      engangsbelopListe.add(
        Engangsbelop(
          engangsbelopId = it.engangsbelopId,
          type = it.type,
          sakId = it.sakId,
          skyldnerId = it.skyldnerId,
          kravhaverId = it.kravhaverId,
          mottakerId = it.mottakerId,
          belop = it.belop,
          valutakode = it.valutakode,
          resultatkode = it.resultatkode,
          referanse = it.referanse,
          endrerEngangsbelopId = it.endrerEngangsbelopId
          )
      )
    }
    return engangsbelopListe
  }
}