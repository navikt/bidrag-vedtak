package no.nav.bidrag.vedtak.api.behandlingsreferanse

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.dto.BehandlingsreferanseDto
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import kotlin.reflect.full.memberProperties

@Schema
data class OpprettBehandlingsreferanseRequest(

  @Schema(description ="Vedtak-id")
  @Min(0)
  val vedtakId: Int,

  @Schema(description ="Kildesystem for behandlingen før vedtaket")
  @NotBlank
  val kilde: String,

  @Schema(description = "Kildesystemets referanse til behandlingen")
  @NotBlank
  val referanse: String
)

fun OpprettBehandlingsreferanseRequest.toBehandlingsreferanseDto(vedtakId: Int) = with(::BehandlingsreferanseDto) {
  val propertiesByName = OpprettBehandlingsreferanseRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      BehandlingsreferanseDto::vedtakId.name -> vedtakId
      BehandlingsreferanseDto::behandlingsreferanseId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toBehandlingsreferanseDto)
    }
  })
}
