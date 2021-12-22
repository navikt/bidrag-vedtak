package no.nav.bidrag.vedtak.api.grunnlag

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank

@Schema
data class OpprettGrunnlagReferanseRequest(

    @Schema(description = "Referanse til grunnlaget")
    @NotBlank
    val grunnlagReferanse: String
)
