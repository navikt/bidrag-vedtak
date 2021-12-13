package no.nav.bidrag.vedtak.api.engangsbelop

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import java.math.BigDecimal
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettEngangsbelopRequest(

  @Schema(description = "Vedtak-id")
  @Min(0)
  val vedtakId: Int,

  @Schema(description = "Løpenr innenfor vedtak")
  @Min(0)
  val lopenr: Int,

  @Schema(description = "Id for eventuelt engangsbeløp som skal endres")
  val endrerEngangsbelopId: Int?,

  @Schema(description = "Beløpstype")
  @NotBlank
  val type: String,

  @Schema(description = "Id til den som skal betale bidraget")
  @NotBlank
  val skyldnerId: String,

  @Schema(description = "Id til den som krever bidraget")
  @NotBlank
  val kravhaverId: String,

  @Schema(description = "Id til den som mottar bidraget")
  @NotBlank
  val mottakerId: String,

  @Schema(description = "Beregnet engangsbeløp")
  @Min(0)
  val belop: BigDecimal,

  @Schema(description = "Valutakoden tilhørende engangsbeløpet")
  @NotBlank
  val valutakode: String,

  @Schema(description = "Resultatkoden tilhørende engangsbeløpet")
  @NotBlank
  val resultatkode: String,
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
