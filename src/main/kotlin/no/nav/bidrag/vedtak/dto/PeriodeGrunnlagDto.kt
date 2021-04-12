package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.PeriodeGrunnlag
import springfox.documentation.spring.web.json.Json
import kotlin.reflect.full.memberProperties

data class PeriodeGrunnlagDto(

  @ApiModelProperty(value = "Periode-id")
  val periodeId: Int = 0,

  @ApiModelProperty(value = "Grunnlag-id")
  val grunnlagId: Int = 0,

  @ApiModelProperty(value = "Grunnlag-valgt")
  val grunnlagValgt: Boolean = true

)

fun PeriodeGrunnlagDto.toPeriodeGrunnlagEntity(eksisterendePeriode: Periode) = with(::PeriodeGrunnlag) {
  val propertiesByName = PeriodeGrunnlagDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      PeriodeGrunnlag::periode.name -> eksisterendePeriode
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeGrunnlagEntity)
    }
  })
}