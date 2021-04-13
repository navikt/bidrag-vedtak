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

  @ApiModelProperty(value = "Periode fra-og-med-dato")
  val periodeFomDato: LocalDate = LocalDate.now(),

  @ApiModelProperty(value = "Periode til-dato")
  val periodeTilDato: LocalDate? = null,

  @ApiModelProperty(value = "Stonadsendring-id")
  val stonadsendringId: Int = 0,

  @ApiModelProperty(value = "Beregnet stønadsbeløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @ApiModelProperty(value = "Valutakoden tilhørende stønadsbeløpet")
  val valutakode: String = "NOK",

  @ApiModelProperty(value = "Resultatkoden tilhørende stønadsbeløpet")
  val resultatkode: String = ""
)

fun PeriodeDto.toPeriodeEntity(eksisterendeStonadsendring: Stonadsendring) = with(::Periode) {
  val propertiesByName = PeriodeDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Periode::stonadsendring.name -> eksisterendeStonadsendring
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeEntity)
    }
  })
}
