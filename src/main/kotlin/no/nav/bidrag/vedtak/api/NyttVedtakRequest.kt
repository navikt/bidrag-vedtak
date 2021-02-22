package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel
data class NyttVedtakRequest(

  @ApiModelProperty(value = "Opprettet av")
  val opprettet_av: String = "NOT SET",

  @ApiModelProperty(value = "Enhetsnummer")
  val enhetsnummer: String = "1111"
)
