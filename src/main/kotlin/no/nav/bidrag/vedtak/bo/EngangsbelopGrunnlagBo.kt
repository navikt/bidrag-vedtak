package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.vedtak.persistence.entity.Engangsbelop
import no.nav.bidrag.vedtak.persistence.entity.EngangsbelopGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import kotlin.reflect.full.memberProperties

@Schema
data class EngangsbelopGrunnlagBo(

  @Schema(description = "Engangsbeløp-id")
  val engangsbelopId: Int,

  @Schema(description = "Grunnlag-id")
  val grunnlagId: Int

)