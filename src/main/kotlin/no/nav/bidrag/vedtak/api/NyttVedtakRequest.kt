package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel
data class NyttVedtakRequest(

  @ApiModelProperty(value = "Id til saksbehandler som oppretter vedtaket")
  val saksbehandlerId: String = "",

  @ApiModelProperty(value = "Id til enheten som er ansvarlig for vedtaket")
  val enhetId: String = ""
)
