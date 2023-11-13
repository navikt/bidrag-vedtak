package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema

@Schema
data class StønadsendringGrunnlagBo(

    @Schema(description = "Stønadsendringsid")
    val stønadsendringsid: Int,

    @Schema(description = "Grunnlagsid")
    val grunnlagsid: Int

)
