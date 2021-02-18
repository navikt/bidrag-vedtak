package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import java.time.LocalDateTime

data class VedtakDto(

  @ApiModelProperty(value = "Vedtak-id")
  val vedtak_id: Int = 0,

  @ApiModelProperty(value = "Opprettet av")
  val opprettet_av: String,

  @ApiModelProperty(value = "Opprettet timestamp")
  val opprettet_timestamp: LocalDateTime = LocalDateTime.now(),

  @ApiModelProperty(value = "Enhetsnummer")
  val enhetsnummer: String
)
