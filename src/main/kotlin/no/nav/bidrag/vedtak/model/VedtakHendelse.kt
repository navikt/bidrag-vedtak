package no.nav.bidrag.vedtak.model

import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.commons.CorrelationId

import java.time.LocalDateTime

data class VedtakHendelse(
  val vedtakId: Int,
  val vedtakType: VedtakType,
  val stonadType: StonadType,
  val sakId: String?,
  val skyldnerId: String,
  val kravhaverId: String,
  val mottakerId: String,
  val opprettetAv: String,
  val opprettetTimestamp: LocalDateTime,
  val periodeListe: List<VedtakHendelsePeriode>
) {
  val sporing: Sporingsdata = Sporingsdata(
    CorrelationId.fetchCorrelationIdForThread() ?: CorrelationId.generateTimestamped(stonadType.toString())
      .get()
  )
}

data class Sporingsdata(val correlationId: String) {
  var brukerident: String? = null

  @Suppress("unused") // brukes av jackson
  val opprettet: LocalDateTime = LocalDateTime.now()
  var opprettetAv: String? = null
}
