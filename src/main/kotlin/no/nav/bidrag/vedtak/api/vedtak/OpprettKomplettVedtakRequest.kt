package no.nav.bidrag.vedtak.api.vedtak

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.api.engangsbelop.OpprettKomplettEngangsbelopRequest
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagRequest
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettKomplettStonadsendringRequest

@ApiModel
data class OpprettKomplettVedtakRequest(

  @ApiModelProperty(value = "Id til saksbehandler som oppretter vedtaket")
  val saksbehandlerId: String = "",

  @ApiModelProperty(value = "Id til enheten som er ansvarlig for vedtaket")
  val enhetId: String = "",

  @ApiModelProperty(value = "Liste over alle grunnlag som inngår i vedtaket")
  val grunnlagListe: List<OpprettGrunnlagRequest> = emptyList(),

  @ApiModelProperty(value = "Liste over alle stønadsendringer som inngår i vedtaket")
  val stonadsendringListe: List<OpprettKomplettStonadsendringRequest> = emptyList(),

  @ApiModelProperty(value = "Liste over alle engangsbeløp som inngår i vedtaket")
  val engangsbelopListe: List<OpprettKomplettEngangsbelopRequest> = emptyList()
)
