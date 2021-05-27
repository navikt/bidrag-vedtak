package no.nav.bidrag.vedtak.api.engangsbelop

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import java.math.BigDecimal
import kotlin.reflect.full.memberProperties

@ApiModel
data class OpprettEngangsbelopRequest(

  @ApiModelProperty(value = "Vedtak-id")
  val vedtakId: Int = 0,

//  @ApiModelProperty(value = "Løpenr innenfor vedtak")
//  val lopenr: Int = 0,

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
)

fun OpprettEngangsbelopRequest.toEngangsbelopDto() = with(::StonadsendringDto) {
  val propertiesByName = OpprettEngangsbelopRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      EngangsbelopDto::engangsbelopId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toEngangsbelopDto)
    }
  })
}
