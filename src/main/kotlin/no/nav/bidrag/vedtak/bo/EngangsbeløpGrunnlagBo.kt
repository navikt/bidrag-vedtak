package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class EngangsbeløpGrunnlagBo(

    @Schema(description = "Engangsbeløp-id")
    val engangsbeløpId: Int,

    @Schema(description = "Grunnlag-id")
    val grunnlagId: Int

)
