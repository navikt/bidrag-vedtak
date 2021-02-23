package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class PeriodeDto(

  @ApiModelProperty(value = "Periode-id")
  val periodeId: Int = 0,

  @ApiModelProperty(value = "Periode fom")
  val periodeFom: LocalDate = LocalDate.now(),

  @ApiModelProperty(value = "Periode tom")
  val periodeTom: LocalDate = LocalDate.now(),

  @ApiModelProperty(value = "Stonad-id")
  val stonadId: Int = 0,

  @ApiModelProperty(value = "Belop")
  val belop: BigDecimal = BigDecimal.ZERO,

  @ApiModelProperty(value = "Opprettet av")
  val opprettetAv: String,

  @ApiModelProperty(value = "Opprettet timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @ApiModelProperty(value = "Enhetsnummer")
  val enhetsnummer: String = ""
)