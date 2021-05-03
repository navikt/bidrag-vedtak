package no.nav.bidrag.vedtak.api.periodegrunnlag

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import kotlin.reflect.full.memberProperties

@ApiModel
data class OpprettPeriodeGrunnlagRequest(

  @ApiModelProperty(value = "Periode-id")
  val periodeId: Int = 0,

  @ApiModelProperty(value = "grunnlag-id")
  val grunnlagId: Int = 0,

  @ApiModelProperty(value = "grunnlag-valgt")
  val grunnlagValgt: Boolean = true
)

fun OpprettPeriodeGrunnlagRequest.toPeriodeGrunnlagDto() = with(::PeriodeGrunnlagDto) {
  val propertiesByName = OpprettPeriodeGrunnlagRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeGrunnlagDto::periodeId.name -> periodeId
      PeriodeGrunnlagDto::grunnlagId.name -> grunnlagId
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeGrunnlagDto)
    }
  })
}
