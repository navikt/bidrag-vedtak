package no.nav.bidrag.vedtak.api.grunnlag

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel
data class HentGrunnlagReferanseResponse(

  @ApiModelProperty(value = "Referanse til grunnlaget")
  val grunnlagReferanse: String = ""
)
