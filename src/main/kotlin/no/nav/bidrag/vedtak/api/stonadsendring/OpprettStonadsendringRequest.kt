package no.nav.bidrag.vedtak.api.stonadsendring

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import kotlin.reflect.full.memberProperties

@ApiModel
data class OpprettStonadsendringRequest(

  @ApiModelProperty(value = "Stønadstype")
  val stonadType: String = "",

  @ApiModelProperty(value = "Vedtak-id")
  val vedtakId: Int = 0,

  @ApiModelProperty(value = "Referanse til sak")
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

fun OpprettStonadsendringRequest.toStonadsendringDto() = with(::StonadsendringDto) {
  val propertiesByName = OpprettStonadsendringRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      StonadsendringDto::stonadsendringId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toStonadsendringDto)
    }
  })
}
