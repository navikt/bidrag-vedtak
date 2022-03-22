package no.nav.bidrag.vedtak.api.grunnlag

import com.fasterxml.jackson.annotation.JsonRawValue
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.enums.GrunnlagType

@Schema
data class HentGrunnlagResponse(

    @Schema(description = "Grunnlag-id")
    val grunnlagId: Int,

    @Schema(description = "Referanse til grunnlaget")
    val referanse: String,

    @Schema(description = "Grunnlagstype")
    val type: GrunnlagType,

    @Schema(description = "Innholdet i grunnlaget")
    @JsonRawValue
    val innhold: String
)
