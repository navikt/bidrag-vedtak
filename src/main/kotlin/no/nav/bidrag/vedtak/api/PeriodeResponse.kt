package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal
import java.time.LocalDate

@ApiModel(value = "Egenskaper ved en periode")
data class PeriodeResponse(

  @ApiModelProperty(value = "Periode fra-og-med-dato")
  val periodeFomDato: LocalDate = LocalDate.now(),

  @ApiModelProperty(value = "Periode til-dato")
  val periodeTilDato: LocalDate? = null,

  @ApiModelProperty(value = "Beregnet stønadsbeløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @ApiModelProperty(value = "Valutakoden tilhørende stønadsbeløpet")
  val valutakode: String = "NOK",

  @ApiModelProperty(value = "Resultatkoden tilhørende  stønadsbeløpet")
  val resultatkode: String = "",

  @ApiModelProperty(value = "Liste over alle grunnlag som inngår i perioden")
  val grunnlagReferanseListe: List<GrunnlagReferanseResponse> = emptyList()
)