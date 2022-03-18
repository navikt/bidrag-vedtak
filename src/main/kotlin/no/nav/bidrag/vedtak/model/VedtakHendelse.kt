package no.nav.bidrag.vedtak.model

import no.nav.bidrag.commons.CorrelationId

import java.time.LocalDateTime

data class VedtakHendelse(
  val vedtakId: Int,
  val vedtakType: String,
  val stonadType: String,
  val sakId: String?,
  val skyldnerId: String,
  val kravhaverId: String,
  val mottakerId: String,
  val opprettetAv: String,
  val opprettetTimestamp: LocalDateTime,
  val periodeListe: List<VedtakHendelsePeriode>
) {
  val sporing: Sporingsdata = Sporingsdata(
    CorrelationId.fetchCorrelationIdForThread() ?: CorrelationId.generateTimestamped(stonadType)
      .get()
  )
}

data class Sporingsdata(val correlationId: String) {
  var brukerident: String? = null

  @Suppress("unused") // brukes av jackson
  val opprettet: LocalDateTime = LocalDateTime.now()
  var opprettetAv: String? = null
}
