package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.PeriodeDto

@ApiModel
data class AllePerioderForStonadResponse(

  @ApiModelProperty(value = "Alle perioder for stonad")
  val allePerioderForStonad: List<PeriodeDto> = emptyList()
)
