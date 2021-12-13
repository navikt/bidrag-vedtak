package no.nav.bidrag.vedtak.api.periode

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagReferanseRequest
import no.nav.bidrag.vedtak.dto.PeriodeDto
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettKomplettPeriodeRequest(

  @Schema(description = "Periode fra-og-med-dato")
  val periodeFomDato: LocalDate,

  @Schema(description = "Periode til-dato")
  val periodeTilDato: LocalDate?,

  @Schema(description = "Stonadsendring-id")
  @Min(0)
  val stonadsendringId: Int,

  @Schema(description = "Beregnet stønadsbeløp")
  @Min(0)
  val belop: BigDecimal,

  @Schema(description = "Valutakoden tilhørende stønadsbeløpet")
  @NotBlank
  val valutakode: String,

  @Schema(description = "Resultatkoden tilhørende stønadsbeløpet")
  @NotBlank
  val resultatkode: String,

  @Schema(description = "Liste over alle grunnlag som inngår i perioden")
  @NotEmpty
  val grunnlagReferanseListe: List<OpprettGrunnlagReferanseRequest>
)

fun OpprettKomplettPeriodeRequest.toPeriodeDto(stonadsendringId: Int) = with(::PeriodeDto) {
  val propertiesByName = OpprettKomplettPeriodeRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeDto::stonadsendringId.name -> stonadsendringId
      PeriodeDto::periodeId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeDto)
    }
  })
}
