package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Periode
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

@ApiModel
data class  NyttPeriodeGrunnlagRequest(

  @ApiModelProperty(value = "Periode-id")
  val periodeId: Int = 0,

  @ApiModelProperty(value = "grunnlag-id")
  val grunnlagId: Int = 0,

  @ApiModelProperty(value = "grunnlag-valgt")
  val grunnlagValgt: Boolean = true
)

fun NyttPeriodeGrunnlagRequest.toPeriodeGrunnlagDto(
  periodeId: Int, grunnlagId: Int) = with(::PeriodeGrunnlagDto) {
  val propertiesByName = NyttPeriodeGrunnlagRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      PeriodeGrunnlagDto::periodeId.name -> periodeId
      PeriodeGrunnlagDto::grunnlagId.name -> grunnlagId
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeGrunnlagDto)
    }
  })
}

fun NyttPeriodeGrunnlagRequest.toPeriodeGrunnlagDto() = with(::PeriodeGrunnlagDto) {
  val propertiesByName = NyttPeriodeGrunnlagRequest::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      PeriodeGrunnlagDto::periodeId.name -> periodeId
      PeriodeGrunnlagDto::grunnlagId.name -> grunnlagId
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeGrunnlagDto)
    }
  })
}