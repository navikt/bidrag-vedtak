package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.persistence.entity.Engangsbelop
import no.nav.bidrag.vedtak.persistence.entity.EngangsbelopGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import kotlin.reflect.full.memberProperties

@Schema
data class EngangsbelopGrunnlagBo(

  @Schema(description = "Engangsbeløp-id")
  val engangsbelopId: Int,

  @Schema(description = "Grunnlag-id")
  val grunnlagId: Int

)

fun EngangsbelopGrunnlagBo.toEngangsbelopGrunnlagEntity(
  eksisterendeEngangsbelop: Engangsbelop, eksisterendeGrunnlag: Grunnlag) = with(::EngangsbelopGrunnlag) {

  val propertiesByName = EngangsbelopGrunnlagBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      EngangsbelopGrunnlag::engangsbelop.name -> eksisterendeEngangsbelop
      EngangsbelopGrunnlag::grunnlag.name -> eksisterendeGrunnlag
      else -> propertiesByName[parameter.name]?.get(this@toEngangsbelopGrunnlagEntity)
    }
  })
}
