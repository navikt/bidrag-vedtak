package no.nav.bidrag.vedtak.api.engangsbelop

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema
data class HentEngangsbelopResponse(

  @Schema(description =  "Id for engangsbeløpet, må returneres for å kunne endres senere")
  val engangsbelopId: Int,

  @Schema(description = "Løpenr innenfor vedtak")
  val lopenr: Int,

  @Schema(description =  "Id for eventuelt engangsbeløp som skal endres")
  val endrerEngangsbelopId: Int? = null,

  @Schema(description =  "Beløpstype. Saertilskudd, gebyr m.m.")
  val type: String,

  @Schema(description =  "Id til den som skal betale engangsbeløpet")
  val skyldnerId: String,

  @Schema(description =  "Id til den som krever engangsbeløpet")
  val kravhaverId: String,

  @Schema(description =  "Id til den som mottar engangsbeløpet")
  val mottakerId: String,

  @Schema(description =  "Beregnet engangsbeløp")
  val belop: BigDecimal,

  @Schema(description =  "Valutakoden tilhørende engangsbeløpet")
  val valutakode: String,

  @Schema(description =  "Resultatkoden tilhørende engangsbeløpet")
  val resultatkode: String,

  @Schema(description =  "Liste over alle grunnlag som inngår i beregningen")
  val grunnlagReferanseListe: List<String> = emptyList()
)
