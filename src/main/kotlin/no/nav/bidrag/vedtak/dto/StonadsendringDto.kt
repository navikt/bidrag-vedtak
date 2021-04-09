package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import kotlin.reflect.full.memberProperties

data class StonadsendringDto(

  @ApiModelProperty(value = "Stønadsendring-id")
  val stonadsendringId: Int = 0,

  @ApiModelProperty(value = "Stønad-type")
  val stonadType: String = "",

  @ApiModelProperty("Vedtaket stønaden gjelder for")
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

fun StonadsendringDto.toStonadsendringEntity(eksisterendeVedtak: Vedtak) = with(::Stonadsendring) {
  val propertiesByName = StonadsendringDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      Stonadsendring::vedtak.name -> eksisterendeVedtak
      else -> propertiesByName[parameter.name]?.get(this@toStonadsendringEntity)
    }
  })
}

