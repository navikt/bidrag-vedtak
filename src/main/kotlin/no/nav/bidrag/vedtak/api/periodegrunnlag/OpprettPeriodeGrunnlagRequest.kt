package no.nav.bidrag.vedtak.api.periodegrunnlag

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettPeriodeGrunnlagRequest(

  @Schema(description = "Periode-id")
  val periodeId: Int = 0,

  @Schema(description = "grunnlag-id")
  val grunnlagId: Int = 0
)

fun OpprettPeriodeGrunnlagRequest.toPeriodeGrunnlagDto() = with(::PeriodeGrunnlagDto) {
  val propertiesByName = OpprettPeriodeGrunnlagRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeGrunnlagDto::periodeId.name -> periodeId
      PeriodeGrunnlagDto::grunnlagId.name -> grunnlagId
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeGrunnlagDto)
    }
  })
}
