package no.nav.bidrag.vedtak.api.vedtak

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class OpprettVedtakRequest(

  @Schema(description = "Id til saksbehandler som oppretter vedtaket")
  val saksbehandlerId: String = "",

  @Schema(description = "Id til enheten som er ansvarlig for vedtaket")
  val enhetId: String = ""
)