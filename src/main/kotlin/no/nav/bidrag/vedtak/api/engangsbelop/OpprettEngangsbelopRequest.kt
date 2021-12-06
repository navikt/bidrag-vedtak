package no.nav.bidrag.vedtak.api.engangsbelop

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import java.math.BigDecimal
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettEngangsbelopRequest(

  @Schema(description = "Vedtak-id")
  val vedtakId: Int = 0,

  @Schema(description = "Løpenr innenfor vedtak")
  val lopenr: Int = 0,

  @Schema(description = "Id for eventuelt engangsbeløp som skal endres")
  val endrerEngangsbelopId: Int? = 0,

  @Schema(description = "Beløpstype")
  val type: String = "",

  @Schema(description = "Id til den som skal betale bidraget")
  val skyldnerId: String = "",

  @Schema(description = "Id til den som krever bidraget")
  val kravhaverId: String = "",

  @Schema(description = "Id til den som mottar bidraget")
  val mottakerId: String = "",

  @Schema(description = "Beregnet engangsbeløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Schema(description = "Valutakoden tilhørende engangsbeløpet")
  val valutakode: String = "NOK",

  @Schema(description = "Resultatkoden tilhørende engangsbeløpet")
  val resultatkode: String = "",
)

fun OpprettEngangsbelopRequest.toEngangsbelopDto() = with(::EngangsbelopDto) {
  val propertiesByName = OpprettEngangsbelopRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      EngangsbelopDto::engangsbelopId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toEngangsbelopDto)
    }
  })
}
