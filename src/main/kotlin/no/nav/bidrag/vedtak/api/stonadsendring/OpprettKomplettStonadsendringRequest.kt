package no.nav.bidrag.vedtak.api.stonadsendring

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.api.periode.OpprettKomplettPeriodeRequest
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettKomplettStonadsendringRequest(

  @Schema(description = "Stønadstype")
  val stonadType: String = "",

  @Schema(description = "Vedtak-id")
  val vedtakId: Int = 0,

  @Schema(description = "Referanse til sak")
  val sakId: String? = null,

  @Schema(description = "Søknadsid, referanse til batchkjøring, fritekst")
  val behandlingId: String? = null,

  @Schema(description = "Id til den som skal betale bidraget")
  val skyldnerId: String = "",

  @Schema(description = "Id til den som krever bidraget")
  val kravhaverId: String = "",

  @Schema(description = "Id til den som mottar bidraget")
  val mottakerId: String = "",

  @Schema(description = "Liste over alle perioder som inngår i stønadsendringen")
  val periodeListe: List<OpprettKomplettPeriodeRequest> = emptyList()
)

fun OpprettKomplettStonadsendringRequest.toStonadsendringDto(vedtakId: Int) = with(::StonadsendringDto) {
  val propertiesByName = OpprettKomplettStonadsendringRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      StonadsendringDto::vedtakId.name -> vedtakId
      StonadsendringDto::stonadsendringId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toStonadsendringDto)
    }
  })
}
