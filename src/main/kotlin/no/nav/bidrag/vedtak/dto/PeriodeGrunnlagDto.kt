package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.persistence.entity.PeriodeGrunnlag
import kotlin.reflect.full.memberProperties

data class PeriodeGrunnlagDto(

  @ApiModelProperty(value = "Periode-id")
  val periodeId: Int = 0,

  @ApiModelProperty(value = "Grunnlag-id")
  val grunnlagId: Int = 0,

  @ApiModelProperty(value = "Er grunnlaget valgt av saksbehandler?")
  val grunnlagValgt: Boolean = true

)

fun PeriodeGrunnlagDto.toPeriodeGrunnlagEntity(eksisterendePeriodeGrunnlag: PeriodeGrunnlag) = with(::PeriodeGrunnlag) {
  val propertiesByName = PeriodeGrunnlagDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    parameter to when (parameter.name) {
      PeriodeGrunnlag::periode.name -> eksisterendePeriodeGrunnlag
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeGrunnlagEntity)
    }
  })
}
