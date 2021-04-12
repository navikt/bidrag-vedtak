package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import kotlin.reflect.full.memberProperties

data class GrunnlagDto(

  @ApiModelProperty(value = "Grunnlag-id")
  val grunnlagId: Int = 0,

  @ApiModelProperty(value = "Referanse til grunnlaget")
  val grunnlagReferanse: String = "",

  @ApiModelProperty(value = "Vedtak-id")
  val vedtakId: Int = 0,

  @ApiModelProperty(value = "Grunnlagstype")
  val grunnlagType: String = "",

  @ApiModelProperty(value = "Innholdet i grunnlaget")
  val grunnlagInnhold: String = ""
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
