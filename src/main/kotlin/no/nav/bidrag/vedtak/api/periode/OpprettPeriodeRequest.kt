package no.nav.bidrag.vedtak.api.periode

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.bo.PeriodeBo
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettPeriodeRequest(

  @Schema(description = "Periode fra-og-med-dato")
  val periodeFomDato: LocalDate,

  @Schema(description = "Periode til-dato")
  val periodeTilDato: LocalDate? = null,

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
  val grunnlagReferanseListe: List<String>
)

fun OpprettPeriodeRequest.toPeriodeDto(stonadsendringId: Int) = with(::PeriodeBo) {
  val propertiesByName = OpprettPeriodeRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeBo::stonadsendringId.name -> stonadsendringId
      PeriodeBo::periodeId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeDto)
    }
  })
}
