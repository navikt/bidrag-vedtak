package no.nav.bidrag.vedtak.api.periodegrunnlag

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import javax.validation.constraints.Min
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettPeriodeGrunnlagRequest(

  @Schema(description = "Periode-id")
  @Min(0)
  val periodeId: Int,

  @Schema(description = "grunnlag-id")
  @Min(0)
  val grunnlagId: Int
)

fun OpprettPeriodeGrunnlagRequest.toPeriodeGrunnlagDto() = with(::PeriodeGrunnlagBo) {
  val propertiesByName = OpprettPeriodeGrunnlagRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeGrunnlagBo::periodeId.name -> periodeId
      PeriodeGrunnlagBo::grunnlagId.name -> grunnlagId
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeGrunnlagDto)
    }
  })
}
