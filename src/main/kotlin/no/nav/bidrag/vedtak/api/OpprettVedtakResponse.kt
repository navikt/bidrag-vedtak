package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Respons ved opprettelse av et vedtak")
data class OpprettVedtakResponse(

  @ApiModelProperty(value = "Id til vedtaket som er opprettet")
  val vedtakId: Int = 0
)
