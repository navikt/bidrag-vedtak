package no.nav.bidrag.vedtak.api.engangsbelop

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagReferanseRequest
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import java.math.BigDecimal
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettKomplettEngangsbelopRequest(

  @Schema(description ="Vedtak-id")
  val vedtakId: Int = 0,

  @Schema(description ="Løpenr innenfor vedtak")
  val lopenr: Int = 0,

  @Schema(description ="Id for eventuelt engangsbeløp som skal endres")
  val endrerEngangsbelopId: Int? = 0,

  @Schema(description ="Beløpstype")
  val type: String = "",

  @Schema(description ="Id til den som skal betale bidraget")
  val skyldnerId: String = "",

  @Schema(description ="Id til den som krever bidraget")
  val kravhaverId: String = "",

  @Schema(description ="Id til den som mottar bidraget")
  val mottakerId: String = "",

  @Schema(description ="Beregnet engangsbeløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Schema(description ="Valutakoden tilhørende engangsbeløpet")
  val valutakode: String = "NOK",

  @Schema(description ="Resultatkoden tilhørende engangsbeløpet")
  val resultatkode: String = "",

  @Schema(description ="Liste over alle grunnlag som inngår i engangsbeløpet")
  val grunnlagReferanseListe: List<OpprettGrunnlagReferanseRequest> = emptyList()
)

fun OpprettKomplettEngangsbelopRequest.toEngangsbelopDto(vedtakId: Int, lopenr: Int) = with(::EngangsbelopDto) {
  val propertiesByName = OpprettKomplettEngangsbelopRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      EngangsbelopDto::vedtakId.name -> vedtakId
      EngangsbelopDto::engangsbelopId.name -> 0
      EngangsbelopDto::lopenr.name -> lopenr
      else -> propertiesByName[parameter.name]?.get(this@toEngangsbelopDto)
    }
  })
}
