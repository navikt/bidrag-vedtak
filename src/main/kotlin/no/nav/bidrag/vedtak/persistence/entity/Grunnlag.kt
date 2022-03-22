package no.nav.bidrag.vedtak.persistence.entity

import no.nav.bidrag.vedtak.dto.GrunnlagDto
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import kotlin.reflect.full.memberProperties

@Entity
data class Grunnlag(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "grunnlag_id")
  val grunnlagId: Int = 0,

  @Column(nullable = false, name = "referanse")
  val referanse: String = "",

  @ManyToOne
  @JoinColumn(name = "vedtak_id")
  val vedtak: Vedtak = Vedtak(),

  @Column(nullable = false, name = "type")
  val type: String = "",

  @Column(nullable = false, name = "innhold")
  val innhold: String = "",

  )

fun Grunnlag.toGrunnlagDto() = with(::GrunnlagDto) {
  val propertiesByName = Grunnlag::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      GrunnlagDto::vedtakId.name -> vedtak.vedtakId
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
    }
  })
}
