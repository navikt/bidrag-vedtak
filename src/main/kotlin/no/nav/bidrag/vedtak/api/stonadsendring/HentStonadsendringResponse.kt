package no.nav.bidrag.vedtak.api.stonadsendring

import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.api.periode.HentPeriodeResponse

data class HentStonadsendringResponse(

  @ApiModelProperty(value = "Stønadstype")
  val stonadType: String = "",

  @ApiModelProperty(value = "Referanse til sak")
  val sakId: String? = null,

  @ApiModelProperty(value = "Søknadsid, referanse til batchkjøring, fritekst")
  val behandlingId: String? = null,

  @ApiModelProperty(value = "Id til den som skal betale bidraget")
  val skyldnerId: String = "",

  @ApiModelProperty(value = "Id til den som krever bidraget")
  val kravhaverId: String = "",

  @ApiModelProperty(value = "Id til den som mottar bidraget")
  val mottakerId: String = "",

  @ApiModelProperty(value = "Liste over alle perioder som inngår i stønadsendringen")
  val periodeListe: List<HentPeriodeResponse> = emptyList()
)