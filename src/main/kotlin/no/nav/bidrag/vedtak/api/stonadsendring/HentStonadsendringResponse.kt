package no.nav.bidrag.vedtak.api.stonadsendring

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.vedtak.api.periode.HentPeriodeResponse

@Schema
data class HentStonadsendringResponse(

  @Schema(description = "Stønadstype")
  val stonadType: StonadType,

  @Schema(description = "Referanse til sak")
  val sakId: String? = null,

  @Schema(description = "Søknadsid, referanse til batchkjøring, fritekst")
  val behandlingId: String? = null,

  @Schema(description = "Id til den som skal betale bidraget")
  val skyldnerId: String,

  @Schema(description = "Id til den som krever bidraget")
  val kravhaverId: String,

  @Schema(description = "Id til den som mottar bidraget")
  val mottakerId: String,

  @Schema(description = "Liste over alle perioder som inngår i stønadsendringen")
  val periodeListe: List<HentPeriodeResponse> = emptyList()
)
