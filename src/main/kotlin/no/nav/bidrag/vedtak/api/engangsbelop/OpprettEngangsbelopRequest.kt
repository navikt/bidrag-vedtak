package no.nav.bidrag.vedtak.api.engangsbelop

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagReferanseRequest
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import java.math.BigDecimal
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettEngangsbelopRequest(

  @Schema(description ="Vedtak-id")
  @Min(0)
  val vedtakId: Int,

  @Schema(description ="Løpenr innenfor vedtak")
  @Min(0)
  val lopenr: Int,

  @Schema(description ="Id for eventuelt engangsbeløp som skal endres")
  val endrerEngangsbelopId: Int?,

  @Schema(description ="Beløpstype")
  @NotBlank
  val type: String,

  @Schema(description ="Id til den som skal betale bidraget")
  @field:Pattern(regexp = "^[0-9]{9}$|^[0-9]{11}$", message = "Ugyldig format. Må inneholde eksakt 9 eller 11 siffer.")
  val skyldnerId: String,

  @Schema(description ="Id til den som krever bidraget")
  @field:Pattern(regexp = "^[0-9]{9}$|^[0-9]{11}$", message = "Ugyldig format. Må inneholde eksakt 9 eller 11 siffer.")
  val kravhaverId: String,

  @Schema(description ="Id til den som mottar bidraget")
  @field:Pattern(regexp = "^[0-9]{9}$|^[0-9]{11}$", message = "Ugyldig format. Må inneholde eksakt 9 eller 11 siffer.")
  val mottakerId: String,

  @Schema(description ="Beregnet engangsbeløp")
  val belop: BigDecimal,

  @Schema(description ="Valutakoden tilhørende engangsbeløpet")
  val valutakode: String,

  @Schema(description ="Resultatkoden tilhørende engangsbeløpet")
  val resultatkode: String,

  @Schema(description ="Liste over alle grunnlag som inngår i engangsbeløpet")
  val grunnlagReferanseListe: List<OpprettGrunnlagReferanseRequest>
)

fun OpprettEngangsbelopRequest.toEngangsbelopDto(vedtakId: Int, lopenr: Int) = with(::EngangsbelopDto) {
  val propertiesByName = OpprettEngangsbelopRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      EngangsbelopDto::vedtakId.name -> vedtakId
      EngangsbelopDto::engangsbelopId.name -> 0
      EngangsbelopDto::lopenr.name -> lopenr
      else -> propertiesByName[parameter.name]?.get(this@toEngangsbelopDto)
    }
  })
}
