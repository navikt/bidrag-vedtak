package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Egenskaper ved et grunnlag")
data class GrunnlagResponse(

  @ApiModelProperty(value = "Grunnlag-id")
  val grunnlagId: Int = 0,

  @ApiModelProperty(value = "Referanse til grunnlaget")
  val grunnlagReferanse: String = "",

  @ApiModelProperty(value = "Grunnlagstype")
  val grunnlagType: String = "",

  @ApiModelProperty(value = "Innholdet i grunnlaget")
  val grunnlagInnhold: String = ""
)