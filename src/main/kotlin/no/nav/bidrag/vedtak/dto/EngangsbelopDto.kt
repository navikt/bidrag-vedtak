package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.persistence.entity.Engangsbelop
import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import java.math.BigDecimal
import kotlin.reflect.full.memberProperties

data class EngangsbelopDto(

  @ApiModelProperty(value = "Engangsbeløp-id")
  val engangsbelopId: Int = 0,

  @ApiModelProperty("Vedtak-id")
  val vedtakId: Int = 0,

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
)

fun EngangsbelopDto.toEngangsbelopEntity(eksisterendeVedtak: Vedtak) = with(::Engangsbelop) {
  val propertiesByName = EngangsbelopDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Engangsbelop::vedtak.name -> eksisterendeVedtak
      else -> propertiesByName[parameter.name]?.get(this@toEngangsbelopEntity)
    }
  })
}
