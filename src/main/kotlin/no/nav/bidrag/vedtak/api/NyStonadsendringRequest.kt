package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

@ApiModel
data class NyStonadsendringRequest(

  @ApiModelProperty(value = "Stønad-type")
  val stonadType: String = "",

  @ApiModelProperty(value = "Vedtak-id")
  val vedtakId: Int = 0,

  @ApiModelProperty(value = "Sak-id")
  val sakId: String = "",

  @ApiModelProperty(value = "Behandling-id")
  val behandlingId: String = "",

  @ApiModelProperty(value = "Skyldner-id")
  val skyldnerId: String = "",

  @ApiModelProperty(value = "Kravhaver-id")
  val kravhaverId: String = "",

  @ApiModelProperty(value = "Mottaker-id")
  val mottakerId: String = ""
)

fun NyStonadsendringRequest.toStonadsendringDto() = with(::StonadsendringDto) {
  val propertiesByName = NyStonadsendringRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      StonadsendringDto::stonadsendringId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toStonadsendringDto)
    }
  })
}
