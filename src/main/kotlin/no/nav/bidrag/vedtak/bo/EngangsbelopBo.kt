package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.enums.EngangsbelopType
import no.nav.bidrag.vedtak.persistence.entity.Engangsbelop
import no.nav.bidrag.vedtak.persistence.entity.EngangsbelopGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import java.math.BigDecimal
import kotlin.reflect.full.memberProperties

@Schema
data class EngangsbelopBo(

  @Schema(description = "Generert id for engangsbeløp")
  val engangsbelopId: Int,

  @Schema(description = "Løpenr innenfor vedtak")
  val lopenr: Int,

  @Schema(description = "Beløpstype. Saertilskudd, gebyr m.m.")
  val type: EngangsbelopType,

  @Schema(description = "Referanse til sak")
  val sakId: String,

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

  @Schema(description = "Referanse - beslutningslinjeId -> bidrag-regnskap")
  val referanse: String?,

  @Schema(description = "Id for eventuelt engangsbeløp som skal endres, skal være id for opprinnelig engangsbeløp")
  val endrerEngangsbelopId: Int?
)


