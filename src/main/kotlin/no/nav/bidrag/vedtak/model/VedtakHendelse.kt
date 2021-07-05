package no.nav.bidrag.vedtak.model

import no.nav.bidrag.commons.CorrelationId

import java.time.LocalDateTime

data class VedtakHendelse(
  val vedtakId: Int = 0,
  val stonadType: String = "",
  val sakId: String? = null,
  val skyldnerId: String = "",
  val kravhaverId: String = "",
  val mottakerId: String = "",
  val opprettetAvSaksbehandlerId: String = "",
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),
  val periodeListe: List<VedtakHendelsePeriode> = emptyList()
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
  var saksbehandlerId: String? = null
}
