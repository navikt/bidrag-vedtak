package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel
data class OppretteNyPeriodeRequest(

  @ApiModelProperty(value = "Opprettet av")
  val opprettet_av: String = "NOT SET",

  @ApiModelProperty(value = "Enhetsnummer")
  val enhetsnummer: String = "3333"
)
