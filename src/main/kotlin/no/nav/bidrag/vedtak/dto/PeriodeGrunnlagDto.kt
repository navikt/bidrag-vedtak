package no.nav.bidrag.vedtak.dto

import io.swagger.annotations.ApiModelProperty

data class PeriodeGrunnlagDto(

  @ApiModelProperty(value = "Periode-id")
  val periodeId: Int = 0,

  @ApiModelProperty(value = "Grunnlag-id")
  val grunnlagId: Int = 0,

  @ApiModelProperty(value = "Er grunnlaget valgt av saksbehandler?")
  val grunnlagValgt: Boolean = true
)
