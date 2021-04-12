package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import kotlin.reflect.full.memberProperties

@ApiModel
data class NyStonadsendringRequest(

  @ApiModelProperty(value = "Stønadstype")
  val stonadType: String = "",

  @ApiModelProperty(value = "Vedtak-id")
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

fun NyStonadsendringRequest.toStonadsendringDto() = with(::StonadsendringDto) {
  val propertiesByName = NyStonadsendringRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      StonadsendringDto::stonadsendringId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toStonadsendringDto)
    }
  })
}
