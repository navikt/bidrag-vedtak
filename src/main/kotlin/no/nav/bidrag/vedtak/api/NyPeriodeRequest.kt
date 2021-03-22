package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.PeriodeDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

@ApiModel
data class  NyPeriodeRequest(

  @ApiModelProperty(value = "Periode-fom")
  val periodeFom: LocalDate = LocalDate.now(),

  @ApiModelProperty(value = "Periode-tom")
  val periodeTom: LocalDate = LocalDate.now(),

  @ApiModelProperty(value = "Stonadsendring-id")
  val stonadsendringId: Int = 0,

  @ApiModelProperty(value = "Belop")
  val belop: BigDecimal = BigDecimal.ZERO,

  @ApiModelProperty(value = "Valutakode")
  val valutakode: String = "",

  @ApiModelProperty(value = "Resultatkode")
  val resultatkode: String = "",

  @ApiModelProperty(value = "Opprettet av")
  val opprettetAv: String = ""

)

fun NyPeriodeRequest.toPeriodeDto() = with(::PeriodeDto) {
  val propertiesByName = NyPeriodeRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      PeriodeDto::periodeId.name -> 0
      PeriodeDto::opprettetTimestamp.name -> LocalDateTime.now()
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeDto)
    }
  })
}