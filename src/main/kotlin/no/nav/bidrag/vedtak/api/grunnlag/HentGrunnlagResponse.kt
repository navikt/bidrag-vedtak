package no.nav.bidrag.vedtak.api.grunnlag

import com.fasterxml.jackson.annotation.JsonRawValue
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel
data class HentGrunnlagResponse(

  @ApiModelProperty(value = "Grunnlag-id")
  val grunnlagId: Int = 0,

  @ApiModelProperty(value = "Referanse til grunnlaget")
  val grunnlagReferanse: String = "",

  @ApiModelProperty(value = "Grunnlagstype")
  val grunnlagType: String = "",

  @ApiModelProperty(value = "Innholdet i grunnlaget")
  @JsonRawValue
  val grunnlagInnhold: String = ""
)
