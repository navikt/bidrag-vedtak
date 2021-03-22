package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.PeriodeDto

@ApiModel
data class AllePerioderForStonadsendringResponse(

  @ApiModelProperty(value = "Alle perioder for stonadsendring")
  val allePerioderForStonadsendring: List<PeriodeDto> = emptyList()
)
