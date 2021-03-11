package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.StonadsendringDto

@ApiModel
data class AlleStonadsendringerResponse(

  @ApiModelProperty(value = "Alle stønadsendringer")
  val alleStonadsendringer: List<StonadsendringDto> = emptyList()
)
