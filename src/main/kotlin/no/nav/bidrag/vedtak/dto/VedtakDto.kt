package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class VedtakDto(

  @ApiModelProperty(value = "Vedtak-id")
  val vedtakId: Int = 0,

  @ApiModelProperty(value = "Id til saksbehandler som oppretter vedtaket")
  val saksbehandlerId: String = "",

  @ApiModelProperty(value = "Id til enheten som er ansvarlig for vedtaket")
  val enhetId: String = "",

  @ApiModelProperty(value = "Opprettet timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now()
)

fun VedtakDto.toVedtakEntity() = with(::Vedtak) {
  val propertiesByName = VedtakDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toVedtakEntity)
    }
  })
}
