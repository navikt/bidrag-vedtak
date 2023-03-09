package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.enums.EngangsbelopType
import no.nav.bidrag.behandling.felles.enums.Innkreving
import java.math.BigDecimal

@Schema
data class EngangsbelopBo(

  @Schema(description = "Generert id for engangsbeløp")
  val id: Int,

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
  val belop: BigDecimal?,

  @Schema(description = "Valutakoden tilhørende engangsbeløpet")
  val valutakode: String?,

  @Schema(description = "Resultatkoden tilhørende engangsbeløpet")
  val resultatkode: String,

  @Schema(description = "Angir om stønaden skal innkreves")
  val innkreving: Innkreving,

  @Schema(description = "Angir om en stønad skal endres som følge av vedtaket")
  val endring: Boolean,

  @Schema(description = "Engangsbeløpet er en endring på engangsbeløp fra vedtakId angitt her")
  val omgjorVedtakId: String?,

  @Schema(description = "Unik referanse (sammen med omgjorVedtakId) for engangsbeløp som er endret")
  val referanse: String?,

  @Schema(description = "Referanse - delytelseId/beslutningslinjeId -> bidrag-regnskap")
  val delytelseId: String?,

  @Schema(description = "Referanse som brukes i utlandssaker")
  val eksternReferanse: String?
)



