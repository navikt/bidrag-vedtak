package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

data class PeriodeDto(

  @ApiModelProperty(value = "Periode-id")
  val periodeId: Int = 0,

  @ApiModelProperty(value = "Periode fom")
  val periodeFom: LocalDate = LocalDate.now(),

  @ApiModelProperty(value = "Periode tom")
  val periodeTom: LocalDate = LocalDate.now(),

  @ApiModelProperty(value = "Stonadsendring-id")
  val stonadsendringId: Int = 0,

  @ApiModelProperty(value = "Belop")
  val belop: BigDecimal = BigDecimal.ZERO,

  @ApiModelProperty(value = "Valutakode")
  val valutakode: String = "",

  @ApiModelProperty(value = "Resultatkode")
  val resultatkode: String = ""
)

fun PeriodeDto.toPeriodeEntity(eksisterendeStonadsendring: Stonadsendring) = with(::Periode) {
  val propertiesByName = PeriodeDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      Periode::stonadsendring.name -> eksisterendeStonadsendring
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeEntity)
    }
  })
}