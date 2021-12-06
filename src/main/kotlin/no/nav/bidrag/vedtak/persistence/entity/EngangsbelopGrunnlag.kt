package no.nav.bidrag.vedtak.persistence.entity

import no.nav.bidrag.vedtak.dto.EngangsbelopGrunnlagDto
import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import kotlin.reflect.full.memberProperties

@IdClass(EngangsbelopGrunnlagPK::class)
@Entity
@Table(name = "engangsbelopgrunnlag")
data class EngangsbelopGrunnlag(

  @Id
  @ManyToOne
  @JoinColumn(name = "engangsbelop_id")
  val engangsbelop: Engangsbelop = Engangsbelop(),

  @Id
  @ManyToOne
  @JoinColumn(name = "grunnlag_id")
  val grunnlag: Grunnlag = Grunnlag()

)

fun EngangsbelopGrunnlag.toEngangsbelopGrunnlagDto() = with(::EngangsbelopGrunnlagDto) {
  val propertiesByName = EngangsbelopGrunnlag::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      EngangsbelopGrunnlagDto::engangsbelopId.name -> engangsbelop.engangsbelopId
      EngangsbelopGrunnlagDto::grunnlagId.name -> grunnlag.grunnlagId
      else -> propertiesByName[parameter.name]?.get(this@toEngangsbelopGrunnlagDto)
    }
  })
}

class EngangsbelopGrunnlagPK(val engangsbelop: Int = 0, val grunnlag: Int = 0) : Serializable
