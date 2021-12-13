package no.nav.bidrag.vedtak.api.vedtak

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.api.behandlingsreferanse.OpprettBehandlingsreferanseRequest
import no.nav.bidrag.vedtak.api.engangsbelop.OpprettKomplettEngangsbelopRequest
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagRequest
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettKomplettStonadsendringRequest
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Schema
data class OpprettKomplettVedtakRequest(

  @Schema(description = "Id til saksbehandler som oppretter vedtaket")
  @Size(min = 7)
  val saksbehandlerId: String,

  @Schema(description = "Dato vedtaket er fattet")
  val vedtakDato: LocalDate?,

  @Schema(description = "Id til enheten som er ansvarlig for vedtaket")
  @NotBlank
  val enhetId: String,

  @Schema(description = "Liste over alle grunnlag som inngår i vedtaket")
  @field:Valid
  val grunnlagListe: List<OpprettGrunnlagRequest>,

  @Schema(description = "Liste over alle stønadsendringer som inngår i vedtaket")
  @field:Valid
  val stonadsendringListe: List<OpprettKomplettStonadsendringRequest>,

  @Schema(description = "Liste over alle engangsbeløp som inngår i vedtaket")
  @field:Valid
  val engangsbelopListe: List<OpprettKomplettEngangsbelopRequest>,

  @Schema(description = "Liste med referanser til alle behandlinger som ligger som grunnlag til vedtaket")
  @field:Valid
  val behandlingsreferanseListe: List<OpprettBehandlingsreferanseRequest>
)
