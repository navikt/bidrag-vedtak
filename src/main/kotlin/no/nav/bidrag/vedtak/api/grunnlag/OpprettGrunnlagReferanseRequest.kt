package no.nav.bidrag.vedtak.api.grunnlag

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class OpprettGrunnlagReferanseRequest(

    @Schema(description = "Referanse til grunnlaget")
    val grunnlagReferanse: String = ""
)
