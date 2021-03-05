package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.StonadDto

@ApiModel
data class AlleStonaderResponse(

  @ApiModelProperty(value = "Alle stønader")
  val alleStonader: List<StonadDto> = emptyList()
)
