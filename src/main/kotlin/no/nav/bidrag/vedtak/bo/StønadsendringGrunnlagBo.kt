package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class StønadsendringGrunnlagBo(

    @Schema(description = "Stønadsendring-id")
    val stønadsendringId: Int,

    @Schema(description = "Grunnlag-id")
    val grunnlagId: Int

)
