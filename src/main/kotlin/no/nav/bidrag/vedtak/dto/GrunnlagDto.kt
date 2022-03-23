package no.nav.bidrag.vedtak.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import kotlin.reflect.full.memberProperties

@Schema
data class GrunnlagDto(

  @Schema(description = "Grunnlag-id")
  val grunnlagId: Int = 0,

  @Schema(description = "Referanse til grunnlaget")
  val referanse: String,

  @Schema(description = "Vedtak-id")
  val vedtakId: Int,

  @Schema(description = "Grunnlagstype")
  val type: String,

  @Schema(description = "Innholdet i grunnlaget")
  val innhold: String
)

fun GrunnlagDto.toGrunnlagEntity(eksisterendeVedtak: Vedtak) = with(::Grunnlag) {
  val propertiesByName = GrunnlagDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Grunnlag::vedtak.name -> eksisterendeVedtak
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagEntity)
    }
  })
}
