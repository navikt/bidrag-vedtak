package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Egenskaper ved et grunnlag")
data class GrunnlagReferanseResponse(

  @ApiModelProperty(value = "Referanse til grunnlaget")
  val grunnlagReferanse: String = "",

  @ApiModelProperty(value = "Er grunnlaget valgt av saksbehandler?")
  val grunnlagValgt: Boolean = true
)
