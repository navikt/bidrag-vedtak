package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettEngangsbelopRequestDto
import no.nav.bidrag.vedtak.persistence.entity.Engangsbelop
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import java.math.BigDecimal
import kotlin.reflect.full.memberProperties

@Schema
data class EngangsbelopBo(

  @Schema(description = "Engangsbeløp-id")
  val engangsbelopId: Int = 0,

  @Schema(description = "Vedtak-id")
  val vedtakId: Int,

  @Schema(description = "Løpenr innenfor vedtak")
  val lopenr: Int,

  @Schema(description = "Id for eventuelt engangsbeløp som skal endres")
  val endrerEngangsbelopId: Int? = null,

  @Schema(description = "Beløpstype. Saertilskudd, gebyr m.m.")
  val type: String,

  @Schema(description = "Id til den som skal betale engangsbeløpet")
  val skyldnerId: String,

  @Schema(description = "Id til den som krever engangsbeløpet")
  val kravhaverId: String,

  @Schema(description = "Id til den som mottar engangsbeløpet")
  val mottakerId: String,

  @Schema(description = "Beregnet engangsbeløp")
  val belop: BigDecimal,

  @Schema(description = "Valutakoden tilhørende engangsbeløpet")
  val valutakode: String,

  @Schema(description = "Resultatkoden tilhørende engangsbeløpet")
  val resultatkode: String,
)


fun OpprettEngangsbelopRequestDto.toEngangsbelopBo(vedtakId: Int, lopenr: Int) = with(::EngangsbelopBo) {
  val propertiesByName = OpprettEngangsbelopRequestDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      EngangsbelopBo::vedtakId.name -> vedtakId
      EngangsbelopBo::engangsbelopId.name -> 0
      EngangsbelopBo::lopenr.name -> lopenr
      else -> propertiesByName[parameter.name]?.get(this@toEngangsbelopBo)
    }
  })
}

fun EngangsbelopBo.toEngangsbelopEntity(eksisterendeVedtak: Vedtak) = with(::Engangsbelop) {
  val propertiesByName = EngangsbelopBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      Engangsbelop::vedtak.name -> eksisterendeVedtak
      else -> propertiesByName[parameter.name]?.get(this@toEngangsbelopEntity)
    }
  })
}
