package no.nav.bidrag.vedtak.persistence.entity

import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import kotlin.reflect.full.memberProperties

@IdClass(PeriodeGrunnlagPK::class)
@Entity
data class PeriodeGrunnlag(

  @Id
  @ManyToOne
  @JoinColumn(name = "periode_id")
  val periode: Periode = Periode(),

  @Id
  @ManyToOne
  @JoinColumn(name = "grunnlag_id")
  val grunnlag: Grunnlag = Grunnlag(),

  @Column(nullable = false, name = "grunnlag_valgt")
  val grunnlagValgt: Boolean = true

  ) //: Serializable

fun PeriodeGrunnlag.toPeriodeGrunnlagDto() = with(::PeriodeGrunnlagDto) {
  val propertiesByName = PeriodeGrunnlag::class.memberProperties.associateBy { it.name }
  callBy(parameters.associate { parameter ->
    parameter to when (parameter.name) {
      PeriodeGrunnlagDto::periodeId.name -> periode.periodeId
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeGrunnlagDto)
    }
  })
}

data class PeriodeGrunnlagPK(val periode: Periode = Periode(), val grunnlag: Grunnlag = Grunnlag()) : Serializable