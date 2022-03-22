package no.nav.bidrag.vedtak.api.stonadsendring

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.vedtak.api.periode.OpprettPeriodeRequest
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettStonadsendringRequest(

  @Schema(description = "Stønadstype")
  @NotBlank
  val stonadType: StonadType,

  @Schema(description = "Vedtak-id")
  @Min(0)
  val vedtakId: Int,

  @Schema(description = "Referanse til sak")
  val sakId: String? = null,

  @Schema(description = "Søknadsid, referanse til batchkjøring, fritekst")
  val behandlingId: String? = null,

  @Schema(description = "Id til den som skal betale bidraget")
  @field:Pattern(regexp = "^[0-9]{9}$|^[0-9]{11}$", message = "Ugyldig format. Må inneholde eksakt 9 eller 11 siffer.")
  val skyldnerId: String,

  @Schema(description = "Id til den som krever bidraget")
  @field:Pattern(regexp = "^[0-9]{9}$|^[0-9]{11}$", message = "Ugyldig format. Må inneholde eksakt 9 eller 11 siffer.")
  val kravhaverId: String,

  @Schema(description = "Id til den som mottar bidraget")
  @field:Pattern(regexp = "^[0-9]{9}$|^[0-9]{11}$", message = "Ugyldig format. Må inneholde eksakt 9 eller 11 siffer.")
  val mottakerId: String,

  @Schema(description = "Liste over alle perioder som inngår i stønadsendringen")
  @field:Valid
  @field:NotEmpty(message = "Listen kan ikke være null eller tom.")
  val periodeListe: List<OpprettPeriodeRequest>
)

fun OpprettStonadsendringRequest.toStonadsendringDto(vedtakId: Int) = with(::StonadsendringDto) {
  val propertiesByName = OpprettStonadsendringRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      StonadsendringDto::vedtakId.name -> vedtakId
      StonadsendringDto::stonadsendringId.name -> 0
      StonadsendringDto::stonadType.name -> stonadType.toString()
      else -> propertiesByName[parameter.name]?.get(this@toStonadsendringDto)
    }
  })
}
