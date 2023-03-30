package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class PeriodeGrunnlagBo(

    @Schema(description = "Periode-id")
    val periodeId: Int,

    @Schema(description = "Grunnlag-id")
    val grunnlagId: Int

)
