package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import kotlin.reflect.full.memberProperties

@ApiModel(value = "Egenskaper ved et grunnlag")
data class NyttGrunnlagRequest(

  @ApiModelProperty(value = "Referanse til grunnlaget")
  val grunnlagReferanse: String = "",

  @ApiModelProperty(value = "Vedtak-id")
  val vedtakId: Int = 0,

  @ApiModelProperty(value = "Grunnlagstype")
  val grunnlagType: String = "",

  @ApiModelProperty(value = "Innholdet i grunnlaget")
  val grunnlagInnhold: String = ""
)

fun NyttGrunnlagRequest.toGrunnlagDto(vedtakId: Int) = with(::GrunnlagDto) {
  val propertiesByName = NyttGrunnlagRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      GrunnlagDto::vedtakId.name -> vedtakId
      GrunnlagDto::grunnlagId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
    }
  })
}

fun NyttGrunnlagRequest.toGrunnlagDto() = with(::GrunnlagDto) {
  val propertiesByName = NyttGrunnlagRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      GrunnlagDto::vedtakId.name -> vedtakId
      GrunnlagDto::grunnlagId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
    }
  })
}
