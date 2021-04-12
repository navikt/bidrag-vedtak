package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import kotlin.reflect.full.memberProperties

data class StonadsendringDto(

  @ApiModelProperty(value = "Stønadsendring-id")
  val stonadsendringId: Int = 0,

  @ApiModelProperty(value = "Stønadstype")
  val stonadType: String = "",

  @ApiModelProperty("Vedtak-id")
  val vedtakId: Int = 0,

  @ApiModelProperty("Referanse til sak")
  val sakId: String? = null,

  @ApiModelProperty(value = "Søknadsid, referanse til batchkjøring, fritekst")
  val behandlingId: String? = null,

  @ApiModelProperty(value = "Id til den som skal betale bidraget")
  val skyldnerId: String = "",

  @ApiModelProperty(value = "Id til den som krever bidraget")
  val kravhaverId: String = "",

  @ApiModelProperty(value = "Id til den som mottar bidraget")
  val mottakerId: String = ""
)

fun StonadsendringDto.toStonadsendringEntity(eksisterendeVedtak: Vedtak) = with(::Stonadsendring) {
  val propertiesByName = StonadsendringDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Stonadsendring::vedtak.name -> eksisterendeVedtak
      else -> propertiesByName[parameter.name]?.get(this@toStonadsendringEntity)
    }
  })
}
