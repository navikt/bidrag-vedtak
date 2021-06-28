package no.nav.bidrag.vedtak.api.vedtak

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.api.engangsbelop.OpprettKomplettEngangsbelopRequest
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagRequest
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettKomplettStonadsendringRequest

@Schema
data class OpprettKomplettVedtakRequest(

  @Schema(description = "Id til saksbehandler som oppretter vedtaket")
  val saksbehandlerId: String = "",

  @Schema(description = "Id til enheten som er ansvarlig for vedtaket")
  val enhetId: String = "",

  @Schema(description = "Liste over alle grunnlag som inngår i vedtaket")
  val grunnlagListe: List<OpprettGrunnlagRequest> = emptyList(),

  @Schema(description = "Liste over alle stønadsendringer som inngår i vedtaket")
  val stonadsendringListe: List<OpprettKomplettStonadsendringRequest> = emptyList(),

  @Schema(description = "Liste over alle engangsbeløp som inngår i vedtaket")
  val engangsbelopListe: List<OpprettKomplettEngangsbelopRequest> = emptyList()

)
