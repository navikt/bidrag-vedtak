package no.nav.bidrag.vedtak.model

import no.nav.bidrag.commons.CorrelationId

import java.time.LocalDateTime


data class VedtakHendelse(
  val stonadType: String = "",
  val sakId: String? = null,
  val skyldnerId: String = "",
  val kravhaverId: String = "",
  val mottakerId: String = "",
  val opprettetAvSaksbehandlerId: String = "",
  val endretAvSaksbehandlerId: String = "",
  val periodeListe: List<VedtakHendelsePeriode> = emptyList()
) {
  val sporing: Sporingsdata = Sporingsdata(
    CorrelationId.fetchCorrelationIdForThread() ?: CorrelationId.generateTimestamped(stonadType)
      .get()
  )

  val opprettetTimestamp: LocalDateTime = LocalDateTime.now()
  val endretTimestamp: LocalDateTime = LocalDateTime.now()

}

data class Sporingsdata(val correlationId: String) {
  var brukerident: String? = null

  @Suppress("unused") // brukes av jackson
  val opprettet: LocalDateTime = LocalDateTime.now()
  var saksbehandlerId: String? = null
}
