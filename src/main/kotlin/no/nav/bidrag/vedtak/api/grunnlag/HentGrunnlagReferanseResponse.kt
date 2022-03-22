package no.nav.bidrag.vedtak.api.grunnlag

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class HentGrunnlagReferanseResponse(

    @Schema(description = "Referanse til grunnlaget")
    val referanse: String
)
