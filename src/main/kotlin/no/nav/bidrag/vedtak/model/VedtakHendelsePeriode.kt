package no.nav.bidrag.vedtak.model

import java.math.BigDecimal
import java.time.LocalDate

data class VedtakHendelsePeriode(
  val periodeFom: LocalDate = LocalDate.now(),
  val periodeTil: LocalDate? = null,
  val belop: BigDecimal = BigDecimal.ZERO,
  val valutakode: String = "NOK",
  val resultatkode: String = "",
)