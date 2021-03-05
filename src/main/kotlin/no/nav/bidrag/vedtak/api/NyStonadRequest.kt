package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.StonadDto
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

@ApiModel
data class NyStonadRequest(

  @ApiModelProperty(value = "Stønad-type")
  val stonadType: String = "",

  @ApiModelProperty(value = "Vedtak-id")
  val vedtakId: Int = 0,

  @ApiModelProperty(value = "Behandling-id")
  val behandlingId: String = "",

  @ApiModelProperty(value = "Skyldner-id")
  val skyldnerId: String = "",

  @ApiModelProperty(value = "Kravhaver-id")
  val kravhaverId: String = "",

  @ApiModelProperty(value = "Mottaker-id")
  val mottakerId: String = "",

  @ApiModelProperty(value = "Opprettet av")
  val opprettetAv: String = "",

  @ApiModelProperty(value = "Enhetsnummer")
  val enhetsnummer: Int = 0
)

fun NyStonadRequest.toStonadDto() = with(::StonadDto) {
  val propertiesByName = NyStonadRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      StonadDto::stonadId.name -> 0
      StonadDto::opprettetTimestamp.name -> LocalDateTime.now()
      else -> propertiesByName[parameter.name]?.get(this@toStonadDto)
    }
  })
}
