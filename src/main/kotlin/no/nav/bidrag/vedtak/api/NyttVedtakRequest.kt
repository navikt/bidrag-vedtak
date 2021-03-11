package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel
data class NyttVedtakRequest(

  @ApiModelProperty(value = "Enhetsnummer")
  val enhetsnummer: String = "",

  @ApiModelProperty(value = "Opprettet av")
  val opprettetAv: String = ""

)
