package no.nav.bidrag.vedtak.persistence.entity

import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakPeriodeGrunnlagRequestDto
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

fun OpprettVedtakPeriodeGrunnlagRequestDto.toPeriodeGrunnlagEntity(eksisterendePeriode: Periode, eksisterendeGrunnlag: Grunnlag) = with(::PeriodeGrunnlag) {
  val propertiesByName = OpprettVedtakPeriodeGrunnlagRequestDto::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      PeriodeGrunnlag::periode.name -> eksisterendePeriode
      PeriodeGrunnlag::grunnlag.name -> eksisterendeGrunnlag
      else -> propertiesByName[parameter.name]?.get(this@toPeriodeGrunnlagEntity)
    }
  })
}

class PeriodeGrunnlagPK(val periode: Int = 0, val grunnlag: Int = 0) : Serializable
