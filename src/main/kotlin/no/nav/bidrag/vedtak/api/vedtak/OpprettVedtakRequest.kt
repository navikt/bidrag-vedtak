package no.nav.bidrag.vedtak.api.vedtak

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.vedtak.api.behandlingsreferanse.OpprettBehandlingsreferanseRequest
import no.nav.bidrag.vedtak.api.engangsbelop.OpprettEngangsbelopRequest
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagRequest
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettStonadsendringRequest
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Schema
data class OpprettVedtakRequest(

  @Schema(description = "Hva slags type vedtak som er fattet")
  val vedtakType: VedtakType,

  @Schema(description = "Id til saksbehandler/batchjobb evt. annet som oppretter vedtaket")
  @Size(min = 5)
  val opprettetAv: String,

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
  val stonadsendringListe: List<OpprettStonadsendringRequest>?,

  @Schema(description = "Liste over alle engangsbeløp som inngår i vedtaket")
  @field:Valid
  val engangsbelopListe: List<OpprettEngangsbelopRequest>?,

  @Schema(description = "Liste med referanser til alle behandlinger som ligger som grunnlag til vedtaket")
  @field:Valid
  val behandlingsreferanseListe: List<OpprettBehandlingsreferanseRequest>?
)
