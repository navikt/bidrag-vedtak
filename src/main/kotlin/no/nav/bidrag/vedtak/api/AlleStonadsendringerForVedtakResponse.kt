package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.StonadsendringDto

@ApiModel
data class AlleStonadsendringerForVedtakResponse(

  @ApiModelProperty(value = "Alle stønadsendringer for et vedtak")
  val alleStonadsendringerForVedtak: List<StonadsendringDto> = emptyList()
)
