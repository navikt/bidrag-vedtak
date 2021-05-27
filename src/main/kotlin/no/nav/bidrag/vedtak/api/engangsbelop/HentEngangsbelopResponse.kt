package no.nav.bidrag.vedtak.api.engangsbelop

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.api.grunnlag.HentGrunnlagReferanseResponse
import no.nav.bidrag.vedtak.api.periode.HentPeriodeResponse
import java.math.BigDecimal

@ApiModel
data class HentEngangsbelopResponse(

  @ApiModelProperty(value = "Løpenr innenfor vedtak")
  val lopenr: Int = 0,

  @ApiModelProperty(value = "Id for eventuelt engangsbeløp som skal endres")
  val endrerEngangsbelopId: Int? = 0,

  @ApiModelProperty(value = "Beløpstype")
  val type: String = "",

  @ApiModelProperty(value = "Id til den som skal betale bidraget")
  val skyldnerId: String = "",

  @ApiModelProperty(value = "Id til den som krever bidraget")
  val kravhaverId: String = "",

  @ApiModelProperty(value = "Id til den som mottar bidraget")
  val mottakerId: String = "",

  @ApiModelProperty(value = "Beregnet engangsbeløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @ApiModelProperty(value = "Valutakoden tilhørende engangsbeløpet")
  val valutakode: String = "NOK",

  @ApiModelProperty(value = "Resultatkoden tilhørende engangsbeløpet")
  val resultatkode: String = "",

  @ApiModelProperty(value = "Liste over alle grunnlag som inngår i beregningen")
  val grunnlagReferanseListe: List<HentGrunnlagReferanseResponse> = emptyList()
)
