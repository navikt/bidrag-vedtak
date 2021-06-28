package no.nav.bidrag.vedtak.model

import java.math.BigDecimal
import java.time.LocalDate

data class VedtakHendelsePeriode(

  val periodeId: Int = 0,
  val periodeFom: LocalDate = LocalDate.now(),
  val periodeTil: LocalDate? = null,
  val stonadId: Int = 0,
  val vedtakId: Int = 0,
  val periodeGjortUgyldigAvVedtakId: Int? = 0,
  val belop: BigDecimal = BigDecimal.ZERO,
  val valutakode: String = "NOK",
  val resultatkode: String = "",
)