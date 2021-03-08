package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.persistence.entity.Stonad
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

data class StonadDto(

  @ApiModelProperty(value = "Stønad-id")
  val stonadId: Int = 0,

  @ApiModelProperty(value = "Stønad-type")
  val stonadType: String = "",

  @ApiModelProperty("Vedtaket stønaden gjelder for")
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

  @ApiModelProperty(value = "Opprettet timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @ApiModelProperty(value = "Enhetsnummer")
  val enhetsnummer: Int = 0
)

fun StonadDto.toStonadEntity(eksisterendeVedtak: Vedtak) = with(::Stonad) {
  val propertiesByName = StonadDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      Stonad::vedtak.name -> eksisterendeVedtak
      else -> propertiesByName[parameter.name]?.get(this@toStonadEntity)
    }
  })
}

