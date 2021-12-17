package no.nav.bidrag.vedtak.api.engangsbelopgrunnlag

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.dto.EngangsbelopGrunnlagDto
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettEngangsbelopGrunnlagRequest(

  @Schema(description ="Engangsbelop-id")
  @Min(0)
  val engangsbelopId: Int,

  @Schema(description ="grunnlag-id")
  @NotBlank
  val grunnlagId: Int
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
