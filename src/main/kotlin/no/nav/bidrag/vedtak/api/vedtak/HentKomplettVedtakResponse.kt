package no.nav.bidrag.vedtak.api.vedtak

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.api.grunnlag.HentGrunnlagResponse
import no.nav.bidrag.vedtak.api.stonadsendring.HentStonadsendringResponse
import java.time.LocalDateTime

@ApiModel
data class HentKomplettVedtakResponse(

  @ApiModelProperty(value = "Vedtak-id")
  var vedtakId: Int = 0,

  @ApiModelProperty(value = "Id til saksbehandler som oppretter vedtaket")
  var saksbehandlerId: String = "",

  @ApiModelProperty(value = "Id til enheten som er ansvarlig for vedtaket")
  var enhetId: String = "",

  @ApiModelProperty(value = "Opprettet timestamp")
  var opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @ApiModelProperty(value = "Liste over alle grunnlag som inngår i vedtaket")
  var grunnlagListe: List<HentGrunnlagResponse> = emptyList(),

  @ApiModelProperty(value = "Liste over alle stønadsendringer som inngår i vedtaket")
  var stonadsendringListe: List<HentStonadsendringResponse> = emptyList()
)
