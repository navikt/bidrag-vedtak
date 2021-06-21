package no.nav.bidrag.vedtak.api.periode

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagReferanseRequest
import no.nav.bidrag.vedtak.dto.PeriodeDto
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettKomplettPeriodeRequest(

  @Schema(description = "Periode fra-og-med-dato")
  val periodeFomDato: LocalDate = LocalDate.now(),

  @Schema(description = "Periode til-dato")
  val periodeTilDato: LocalDate? = null,

  @Schema(description = "Stonadsendring-id")
  val stonadsendringId: Int = 0,

  @Schema(description = "Beregnet stønadsbeløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Schema(description = "Valutakoden tilhørende stønadsbeløpet")
  val valutakode: String = "NOK",

  @Schema(description = "Resultatkoden tilhørende  stønadsbeløpet")
  val resultatkode: String = "",

  @Schema(description = "Liste over alle grunnlag som inngår i perioden")
  val grunnlagReferanseListe: List<OpprettGrunnlagReferanseRequest> = emptyList()
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
