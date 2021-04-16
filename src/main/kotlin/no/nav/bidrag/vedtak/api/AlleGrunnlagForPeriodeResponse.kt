package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto

@ApiModel
data class AlleGrunnlagForPeriodeResponse(

  @ApiModelProperty(value = "Alle grunnlag for en periode")
  val alleGrunnlagForPeriode: List<PeriodeGrunnlagDto> = emptyList()
)
