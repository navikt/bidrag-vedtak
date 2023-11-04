package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class EngangsbeløpGrunnlagBo(

    @Schema(description = "Engangsbeløpsid")
    val engangsbeløpsid: Int,

    @Schema(description = "Grunnlagsid")
    val grunnlagsid: Int

)
