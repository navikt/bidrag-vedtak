package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel
data class NyPeriodeRequest(

  @ApiModelProperty(value = "Opprettet av")
  val opprettetAv: String = "NOT SET",

  @ApiModelProperty(value = "Enhetsnummer")
  val enhetsnummer: String = "3333"
)
