package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettStonadsendringRequestDto
import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import kotlin.reflect.full.memberProperties

@Schema
data class StonadsendringBo(

  @Schema(description = "Stønadsendring-id")
  val stonadsendringId: Int = 0,

  @Schema(description = "Stønadstype")
  val stonadType: String,

  @Schema(description ="Vedtak-id")
  val vedtakId: Int,

  @Schema(description ="Referanse til sak")
  val sakId: String? = null,

  @Schema(description = "Søknadsid, referanse til batchkjøring, fritekst")
  val behandlingId: String? = null,

  @Schema(description = "Id til den som skal betale bidraget")
  val skyldnerId: String,

  @Schema(description = "Id til den som krever bidraget")
  val kravhaverId: String,

  @Schema(description = "Id til den som mottar bidraget")
  val mottakerId: String
)

fun OpprettStonadsendringRequestDto.toStonadsendringBo(vedtakId: Int) = with(::StonadsendringBo) {
  val propertiesByName = OpprettStonadsendringRequestDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      StonadsendringBo::vedtakId.name -> vedtakId
      StonadsendringBo::stonadsendringId.name -> 0
      StonadsendringBo::stonadType.name -> stonadType.toString()
      else -> propertiesByName[parameter.name]?.get(this@toStonadsendringBo)
    }
  })
}

fun StonadsendringBo.toStonadsendringEntity(eksisterendeVedtak: Vedtak) = with(::Stonadsendring) {
  val propertiesByName = StonadsendringBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Stonadsendring::vedtak.name -> eksisterendeVedtak
      else -> propertiesByName[parameter.name]?.get(this@toStonadsendringEntity)
    }
  })
}
