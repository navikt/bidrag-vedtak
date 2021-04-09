package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import springfox.documentation.spring.web.json.Json
import kotlin.reflect.full.memberProperties

data class GrunnlagDto(

  @ApiModelProperty(value = "Grunnlag-id")
  val grunnlagId: Int = 0,

  @ApiModelProperty(value = "Grunnlag-referanse")
  val grunnlagReferanse: String = "",

  @ApiModelProperty("Vedtaket grunnlaget gjelder for")
  val vedtakId: Int = 0,

  @ApiModelProperty(value = "Grunnlag-type")
  val grunnlagType: String = "",

  @ApiModelProperty(value = "Grunnlag-innhold")
  val grunnlagInnhold: String = ""

)

fun GrunnlagDto.toGrunnlagEntity(eksisterendeVedtak: Vedtak) = with(::Grunnlag) {
  val propertiesByName = GrunnlagDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      Grunnlag::vedtak.name -> eksisterendeVedtak
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagEntity)
    }
  })
}