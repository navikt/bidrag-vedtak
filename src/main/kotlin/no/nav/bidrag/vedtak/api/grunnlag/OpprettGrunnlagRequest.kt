package no.nav.bidrag.vedtak.api.grunnlag

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import kotlin.reflect.full.memberProperties

@ApiModel
data class OpprettGrunnlagRequest(

  @ApiModelProperty(value = "Referanse til grunnlaget")
  val grunnlagReferanse: String = "",

  @ApiModelProperty(value = "Vedtak-id")
  val vedtakId: Int = 0,

  @ApiModelProperty(value = "Grunnlagstype")
  val grunnlagType: String = "",

  @ApiModelProperty(value = "Innholdet i grunnlaget")
  val grunnlagInnhold: JsonNode = ObjectMapper().createObjectNode()
)

fun OpprettGrunnlagRequest.toGrunnlagDto(vedtakId: Int) = with(::GrunnlagDto) {
  val propertiesByName = OpprettGrunnlagRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      GrunnlagDto::vedtakId.name -> vedtakId
      GrunnlagDto::grunnlagId.name -> 0
      GrunnlagDto::grunnlagInnhold.name -> grunnlagInnhold.toString()
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
    }
  })
}

fun OpprettGrunnlagRequest.toGrunnlagDto() = with(::GrunnlagDto) {
  val propertiesByName = OpprettGrunnlagRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      GrunnlagDto::grunnlagId.name -> 0
      GrunnlagDto::grunnlagInnhold.name -> grunnlagInnhold.toString()
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
    }
  })
}
