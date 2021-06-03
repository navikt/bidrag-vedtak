package no.nav.bidrag.vedtak.api.engangsbelopgrunnlag

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.EngangsbelopGrunnlagDto
import kotlin.reflect.full.memberProperties

@ApiModel
data class OpprettEngangsbelopGrunnlagRequest(

  @ApiModelProperty(value = "Engangsbelop-id")
  val engangsbelopId: Int = 0,

  @ApiModelProperty(value = "grunnlag-id")
  val grunnlagId: Int = 0
)

fun OpprettEngangsbelopGrunnlagRequest.toEngangsbelopGrunnlagDto() = with(::EngangsbelopGrunnlagDto) {
  val propertiesByName = OpprettEngangsbelopGrunnlagRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      EngangsbelopGrunnlagDto::engangsbelopId.name -> engangsbelopId
      EngangsbelopGrunnlagDto::grunnlagId.name -> grunnlagId
      else -> propertiesByName[parameter.name]?.get(this@toEngangsbelopGrunnlagDto)
    }
  })
}
