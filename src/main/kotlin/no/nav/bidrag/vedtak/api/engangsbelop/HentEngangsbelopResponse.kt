package no.nav.bidrag.vedtak.api.engangsbelop

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.api.grunnlag.HentGrunnlagReferanseResponse
import java.math.BigDecimal

@Schema
data class HentEngangsbelopResponse(

  @Schema(description =  "Id for engangsbeløpet, må returneres for å kunne endres senere")
  val engangsbelopId: Int = 0,

  @Schema(description =  "Løpenr innenfor vedtak")
  val lopenr: Int = 0,

  @Schema(description =  "Id for eventuelt engangsbeløp som skal endres")
  val endrerEngangsbelopId: Int? = 0,

  @Schema(description =  "Beløpstype")
  val type: String = "",

  @Schema(description =  "Id til den som skal betale bidraget")
  val skyldnerId: String = "",

  @Schema(description =  "Id til den som krever bidraget")
  val kravhaverId: String = "",

  @Schema(description =  "Id til den som mottar bidraget")
  val mottakerId: String = "",

  @Schema(description =  "Beregnet engangsbeløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Schema(description =  "Valutakoden tilhørende engangsbeløpet")
  val valutakode: String = "NOK",

  @Schema(description =  "Resultatkoden tilhørende engangsbeløpet")
  val resultatkode: String = "",

  @Schema(description =  "Liste over alle grunnlag som inngår i beregningen")
  val grunnlagReferanseListe: List<HentGrunnlagReferanseResponse> = emptyList()
)
