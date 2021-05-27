package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.persistence.entity.Engangsbelop
import no.nav.bidrag.vedtak.persistence.entity.EngangsbelopGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import kotlin.reflect.full.memberProperties

data class EngangsbelopGrunnlagDto(

  @ApiModelProperty(value = "Engangsbeløp-id")
  val engangsbelopId: Int = 0,

  @ApiModelProperty(value = "Grunnlag-id")
  val grunnlagId: Int = 0

)

fun EngangsbelopGrunnlagDto.toEngangsbelopGrunnlagEntity(
  eksisterendeEngangsbelop: Engangsbelop, eksisterendeGrunnlag: Grunnlag) = with(::EngangsbelopGrunnlag) {

  val propertiesByName = EngangsbelopGrunnlagDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      EngangsbelopGrunnlag::engangsbelop.name -> eksisterendeEngangsbelop
      EngangsbelopGrunnlag::grunnlag.name -> eksisterendeGrunnlag
      else -> propertiesByName[parameter.name]?.get(this@toEngangsbelopGrunnlagEntity)
    }
  })
}
