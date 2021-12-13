package no.nav.bidrag.vedtak.api.periodegrunnlag

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
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
