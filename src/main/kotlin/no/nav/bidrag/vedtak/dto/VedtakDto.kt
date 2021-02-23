package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import java.time.LocalDateTime

data class VedtakDto(

  @ApiModelProperty(value = "Vedtak-id")
  val vedtakId: Int = 0,

  @ApiModelProperty(value = "Opprettet av")
  val opprettetAv: String = "",

  @ApiModelProperty(value = "Opprettet timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @ApiModelProperty(value = "Enhetsnummer")
  val enhetsnummer: String = ""
)
