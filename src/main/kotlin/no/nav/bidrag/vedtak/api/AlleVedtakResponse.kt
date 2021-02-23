package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.VedtakDto

@ApiModel
data class AlleVedtakResponse(

  @ApiModelProperty(value = "Alle vedtak")
  val alleVedtak: List<VedtakDto> = emptyList()
)
