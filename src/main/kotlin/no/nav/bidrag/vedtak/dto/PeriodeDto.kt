package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class PeriodeDto(

  @ApiModelProperty(value = "Periode-id")
  val periode_id: Int = 0,

  @ApiModelProperty(value = "Periode fom")
  val periode_fom: LocalDate = LocalDate.now(),

  @ApiModelProperty(value = "Periode tom")
  val periode_tom: LocalDate = LocalDate.now(),

  @ApiModelProperty(value = "Stonad-id")
  val stonad_id: Int = 0,

  @ApiModelProperty(value = "Belop")
  val belop: BigDecimal = BigDecimal.ZERO,

  @ApiModelProperty(value = "Opprettet av")
  val opprettet_av: String,

  @ApiModelProperty(value = "Opprettet timestamp")
  val opprettet_timestamp: LocalDateTime = LocalDateTime.now(),

  @ApiModelProperty(value = "Enhetsnummer")
  val enhetsnummer: String
)