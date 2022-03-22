package no.nav.bidrag.vedtak.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

@Schema
data class VedtakDto(

  @Schema(description = "Vedtak-id")
  val vedtakId: Int = 0,

  @Schema(description = "Vedtak-type. Typisk manuelt eller en batchjobb")
  val vedtakType: String = "",

  @Schema(description = "Id til enheten som er ansvarlig for vedtaket")
  val enhetId: String = "",

  @Schema(description = "Dato vedtaket er fattet")
  val vedtakDato: LocalDate? = null,

  @Schema(description = "Id til saksbehandler/batchjobb evt. annet som oppretter vedtaket")
  val opprettetAv: String = "",

  @Schema(description = "Opprettet timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now()
)

fun VedtakDto.toVedtakEntity() = with(::Vedtak) {
  val propertiesByName = VedtakDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toVedtakEntity)
    }
  })
}
