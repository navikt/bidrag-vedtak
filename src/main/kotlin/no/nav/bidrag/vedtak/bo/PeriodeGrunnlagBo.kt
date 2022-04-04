package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.PeriodeGrunnlag
import kotlin.reflect.full.memberProperties

@Schema
data class PeriodeGrunnlagBo(

  @Schema(description = "Periode-id")
  val periodeId: Int,

  @Schema(description = "Grunnlag-id")
  val grunnlagId: Int

)

fun PeriodeGrunnlagBo.toPeriodeGrunnlagEntity(eksisterendePeriode: Periode, eksisterendeGrunnlag: Grunnlag) = with(::PeriodeGrunnlag) {
  val propertiesByName = PeriodeGrunnlagBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeGrunnlag::periode.name -> eksisterendePeriode
      PeriodeGrunnlag::grunnlag.name -> eksisterendeGrunnlag
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeGrunnlagEntity)
    }
  })
}
