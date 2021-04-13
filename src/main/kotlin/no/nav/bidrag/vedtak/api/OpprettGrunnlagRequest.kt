package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import kotlin.reflect.full.memberProperties

@ApiModel(value = "Egenskaper ved et grunnlag")
data class OpprettGrunnlagRequest(

  @ApiModelProperty(value = "Referanse til grunnlaget")
  val grunnlagReferanse: String = "",

  @ApiModelProperty(value = "Grunnlagstype")
  val grunnlagType: String = "",

  @ApiModelProperty(value = "Innholdet i grunnlaget")
  val grunnlagInnhold: String = ""
)

fun OpprettGrunnlagRequest.toGrunnlagDto(vedtakId: Int) = with(::GrunnlagDto) {
  val propertiesByName = OpprettGrunnlagRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      GrunnlagDto::vedtakId.name -> vedtakId
      GrunnlagDto::grunnlagId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
    }
  })
}
