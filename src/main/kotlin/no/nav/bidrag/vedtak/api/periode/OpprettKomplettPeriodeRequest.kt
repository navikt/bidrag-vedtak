package no.nav.bidrag.vedtak.api.periode

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagReferanseRequest
import no.nav.bidrag.vedtak.dto.PeriodeDto
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

@ApiModel
data class OpprettKomplettPeriodeRequest(

  @ApiModelProperty(value = "Periode fra-og-med-dato")
  val periodeFomDato: LocalDate = LocalDate.now(),

  @ApiModelProperty(value = "Periode til-dato")
  val periodeTilDato: LocalDate? = null,

  @ApiModelProperty(value = "Stonadsendring-id")
  val stonadsendringId: Int = 0,

  @ApiModelProperty(value = "Beregnet stønadsbeløp")
  val belop: BigDecimal = BigDecimal.ZERO,

  @ApiModelProperty(value = "Valutakoden tilhørende stønadsbeløpet")
  val valutakode: String = "NOK",

  @ApiModelProperty(value = "Resultatkoden tilhørende  stønadsbeløpet")
  val resultatkode: String = "",

  @ApiModelProperty(value = "Liste over alle stønadsendringer som inngår i vedtaket")
  val grunnlagReferanseListe: List<OpprettGrunnlagReferanseRequest> = emptyList()
)

fun OpprettKomplettPeriodeRequest.toPeriodeDto(stonadsendringId: Int) = with(::PeriodeDto) {
  val propertiesByName = OpprettKomplettPeriodeRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeDto::stonadsendringId.name -> stonadsendringId
      PeriodeDto::periodeId.name -> 0
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeDto)
    }
  })
}
