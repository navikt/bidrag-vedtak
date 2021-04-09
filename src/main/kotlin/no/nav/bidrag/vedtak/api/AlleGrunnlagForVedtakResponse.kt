package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.GrunnlagDto

@ApiModel
data class AlleGrunnlagForVedtakResponse(

  @ApiModelProperty(value = "Alle grunnlag for et vedtak")
  val alleGrunnlagForVedtak: List<GrunnlagDto> = emptyList()
)
