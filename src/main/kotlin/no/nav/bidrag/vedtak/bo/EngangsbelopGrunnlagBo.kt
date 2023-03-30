package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class EngangsbelopGrunnlagBo(

    @Schema(description = "Engangsbeløp-id")
    val engangsbelopId: Int,

    @Schema(description = "Grunnlag-id")
    val grunnlagId: Int

)
