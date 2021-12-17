package no.nav.bidrag.vedtak.api.vedtak

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Schema
data class OpprettVedtakRequest(

  @Schema(description = "Id til saksbehandler som oppretter vedtaket")
  @Size(min = 7)
  val saksbehandlerId: String,

  @Schema(description = "Id til enheten som er ansvarlig for vedtaket")
  @NotBlank
  val enhetId: String
)