package no.nav.bidrag.vedtak.api.periode

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.api.grunnlag.HentGrunnlagReferanseResponse
import java.math.BigDecimal
import java.time.LocalDate

@Schema
data class HentPeriodeResponse(

  @Schema(description = "Periode fra-og-med-dato")
  val periodeFomDato: LocalDate = LocalDate.now(),

  @Schema(description = "Periode til-dato")
  val periodeTilDato: LocalDate? = null,

  @Schema(description = "Beregnet stønadsbeløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Schema(description = "Valutakoden tilhørende stønadsbeløpet")
  val valutakode: String = "NOK",

  @Schema(description = "Resultatkoden tilhørende  stønadsbeløpet")
  val resultatkode: String = "",

  @Schema(description = "Liste over alle grunnlag som inngår i perioden")
  val grunnlagReferanseListe: List<HentGrunnlagReferanseResponse> = emptyList()
)
