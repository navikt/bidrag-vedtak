package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class VedtakDto(

  @ApiModelProperty(value = "Vedtak-id")
  val vedtakId: Int = 0,

  @ApiModelProperty(value = "Opprettet av")
  val opprettetAv: String = "",

  @ApiModelProperty(value = "Opprettet timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @ApiModelProperty(value = "Enhetsnummer")
  val enhetsnummer: String = ""
)

fun VedtakDto.toVedtakEntity() = with(::Vedtak) {
  val propertiesByName = VedtakDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toVedtakEntity)
    }
  })
}