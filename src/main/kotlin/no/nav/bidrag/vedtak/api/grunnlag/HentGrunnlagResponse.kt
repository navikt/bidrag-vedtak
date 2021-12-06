package no.nav.bidrag.vedtak.api.grunnlag

import com.fasterxml.jackson.annotation.JsonRawValue
import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class HentGrunnlagResponse(

    @Schema(description = "Grunnlag-id")
    val grunnlagId: Int = 0,

    @Schema(description = "Referanse til grunnlaget")
    val grunnlagReferanse: String = "",

    @Schema(description = "Grunnlagstype")
    val grunnlagType: String = "",

    @Schema(description = "Innholdet i grunnlaget")
    @JsonRawValue
    val grunnlagInnhold: String = ""
)
