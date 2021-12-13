package no.nav.bidrag.vedtak.api.stonadsendring

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.api.periode.OpprettKomplettPeriodeRequest
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettKomplettStonadsendringRequest(

  @Schema(description = "Stønadstype")
  @NotBlank
  val stonadType: String,

  @Schema(description = "Vedtak-id")
  @Min(0)
  val vedtakId: Int,

  @Schema(description = "Referanse til sak")
  val sakId: String?,

  @Schema(description = "Søknadsid, referanse til batchkjøring, fritekst")
  val behandlingId: String?,

  @Schema(description = "Id til den som skal betale bidraget")
  @NotBlank
  val skyldnerId: String,

  @Schema(description = "Id til den som krever bidraget")
  @NotBlank
  val kravhaverId: String,

  @Schema(description = "Id til den som mottar bidraget")
  @NotBlank
  val mottakerId: String,

  @Schema(description = "Liste over alle perioder som inngår i stønadsendringen")
  @field:Valid
  @field:NotEmpty(message = "Listen kan ikke være null eller tom.")
  val periodeListe: List<OpprettKomplettPeriodeRequest>
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
