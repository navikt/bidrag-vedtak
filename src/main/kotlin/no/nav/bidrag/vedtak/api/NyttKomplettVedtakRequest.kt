package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Egenskaper ved et vedtak")
data class NyttKomplettVedtakRequest(

  @ApiModelProperty(value = "Id til saksbehandler som oppretter vedtaket")
  val saksbehandlerId: String = "",

  @ApiModelProperty(value = "Id til enheten som er ansvarlig for vedtaket")
  val enhetId: String = "",

  @ApiModelProperty(value = "Liste over alle grunnlag som inngår i vedtaket")
  val grunnlagListe: List<NyttGrunnlagRequest> = emptyList(),

  @ApiModelProperty(value = "Liste over alle stønadsendringer som inngår i vedtaket")
  val stonadsendringListe: List<NyStonadsendringRequest> = emptyList()
)
