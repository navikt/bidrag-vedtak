package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

@Schema
data class PeriodeBo(

  @Schema(description = "Periode-id")
  val periodeId: Int = 0,

  @Schema(description = "Periode fra-og-med-dato")
  val periodeFomDato: LocalDate,

  @Schema(description = "Periode til-dato")
  val periodeTilDato: LocalDate? = null,

  @Schema(description = "Stonadsendring-id")
  val stonadsendringId: Int,

  @Schema(description = "Beregnet stønadsbeløp")
  val belop: BigDecimal,

  @Schema(description = "Valutakoden tilhørende stønadsbeløpet")
  val valutakode: String,

  @Schema(description = "Resultatkoden tilhørende stønadsbeløpet")
  val resultatkode: String
)

fun PeriodeBo.toPeriodeEntity(eksisterendeStonadsendring: Stonadsendring) = with(::Periode) {
  val propertiesByName = PeriodeBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Periode::stonadsendring.name -> eksisterendeStonadsendring
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeEntity)
    }
  })
}
