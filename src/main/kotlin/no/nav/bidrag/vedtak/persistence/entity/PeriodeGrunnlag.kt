package no.nav.bidrag.vedtak.persistence.entity

import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import kotlin.reflect.full.memberProperties

@IdClass(PeriodeGrunnlagPK::class)
@Entity
@Table(name = "periodegrunnlag")
data class PeriodeGrunnlag(

  @Id
  @ManyToOne
  @JoinColumn(name = "periode_id")
  val periode: Periode = Periode(),

  @Id
  @ManyToOne
  @JoinColumn(name = "grunnlag_id")
  val grunnlag: Grunnlag = Grunnlag()

)

fun PeriodeGrunnlag.toPeriodeGrunnlagDto() = with(::PeriodeGrunnlagBo) {
  val propertiesByName = PeriodeGrunnlag::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeGrunnlagBo::periodeId.name -> periode.periodeId
      PeriodeGrunnlagBo::grunnlagId.name -> grunnlag.grunnlagId
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeGrunnlagDto)
    }
  })
}

class PeriodeGrunnlagPK(val periode: Int = 0, val grunnlag: Int = 0) : Serializable
