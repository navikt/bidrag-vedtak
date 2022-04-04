package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettBehandlingsreferanseRequestDto
import no.nav.bidrag.vedtak.persistence.entity.Behandlingsreferanse
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import kotlin.reflect.full.memberProperties

@Schema
data class BehandlingsreferanseBo(

  @Schema(description = "Behandlingsreferanse-id")
  val behandlingsreferanseId: Int = 0,

  @Schema(description ="Vedtak-id")
  val vedtakId: Int,

  @Schema(description ="Kildesystem for behandlingen før vedtaket")
  val kilde: String,

  @Schema(description = "Kildesystemets referanse til behandlingen")
  val referanse: String
)


fun OpprettBehandlingsreferanseRequestDto.toBehandlingsreferanseBo(vedtakId: Int) = with(::BehandlingsreferanseBo) {
  val propertiesByName = OpprettBehandlingsreferanseRequestDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      BehandlingsreferanseBo::vedtakId.name -> vedtakId
      BehandlingsreferanseBo::behandlingsreferanseId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toBehandlingsreferanseBo)
    }
  })
}


fun BehandlingsreferanseBo.toBehandlingsreferanseEntity(eksisterendeVedtak: Vedtak) = with(::Behandlingsreferanse) {
  val propertiesByName = BehandlingsreferanseBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Behandlingsreferanse::vedtak.name -> eksisterendeVedtak
      else -> propertiesByName[parameter.name]?.get(this@toBehandlingsreferanseEntity)
    }
  })
}
