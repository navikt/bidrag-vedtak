package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettGrunnlagRequestDto
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import kotlin.reflect.full.memberProperties

@Schema
data class GrunnlagBo(

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

fun OpprettGrunnlagRequestDto.toGrunnlagBo(vedtakId: Int) = with(::GrunnlagBo) {
  val propertiesByName = OpprettGrunnlagRequestDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      GrunnlagBo::vedtakId.name -> vedtakId
      GrunnlagBo::grunnlagId.name -> 0
      GrunnlagBo::type.name -> type.toString()
      GrunnlagBo::innhold.name -> innhold.toString()
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagBo)
    }
  })
}

fun GrunnlagBo.toGrunnlagEntity(eksisterendeVedtak: Vedtak) = with(::Grunnlag) {
  val propertiesByName = GrunnlagBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Grunnlag::vedtak.name -> eksisterendeVedtak
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagEntity)
    }
  })
}
