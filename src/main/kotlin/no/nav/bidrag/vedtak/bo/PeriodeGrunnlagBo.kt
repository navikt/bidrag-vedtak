package no.nav.bidrag.vedtak.bo

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakPeriodeGrunnlagRequestDto
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.PeriodeGrunnlag
import kotlin.reflect.full.memberProperties

@Schema
data class PeriodeGrunnlagBo(

  @Schema(description = "Periode-id")
  val periodeId: Int,

  @Schema(description = "Grunnlag-id")
  val grunnlagId: Int

)


fun OpprettVedtakPeriodeGrunnlagRequestDto.toPeriodeGrunnlagBo() = with(::PeriodeGrunnlagBo) {
  val propertiesByName = OpprettVedtakPeriodeGrunnlagRequestDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeGrunnlagBo::periodeId.name -> periodeId
      PeriodeGrunnlagBo::grunnlagId.name -> grunnlagId
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeGrunnlagBo)
    }
  })
}


fun PeriodeGrunnlagBo.toPeriodeGrunnlagEntity(eksisterendePeriode: Periode, eksisterendeGrunnlag: Grunnlag) = with(::PeriodeGrunnlag) {
  val propertiesByName = PeriodeGrunnlagBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeGrunnlag::periode.name -> eksisterendePeriode
      PeriodeGrunnlag::grunnlag.name -> eksisterendeGrunnlag
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeGrunnlagEntity)
    }
  })
}
