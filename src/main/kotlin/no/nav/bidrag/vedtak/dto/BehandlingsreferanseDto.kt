package no.nav.bidrag.vedtak.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.persistence.entity.Behandlingsreferanse
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import kotlin.reflect.full.memberProperties

@Schema
data class BehandlingsreferanseDto(

  @Schema(description = "Behandlingsreferanse-id")
  val behandlingsreferanseId: Int = 0,

  @Schema(description ="Vedtak-id")
  val vedtakId: Int = 0,

  @Schema(description ="Kildesystem for behandlingen før vedtaket")
  val kilde: String = "",

  @Schema(description = "Kildesystemets referanse til behandlingen")
  val referanse: String = ""
)

fun BehandlingsreferanseDto.toBehandlingsreferanseEntity(eksisterendeVedtak: Vedtak) = with(::Behandlingsreferanse) {
  val propertiesByName = BehandlingsreferanseDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Behandlingsreferanse::vedtak.name -> eksisterendeVedtak
      else -> propertiesByName[parameter.name]?.get(this@toBehandlingsreferanseEntity)
    }
  })
}
